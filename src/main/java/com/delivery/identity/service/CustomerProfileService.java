package com.delivery.identity.service;

import com.delivery.identity.dto.AddressDto;
import com.delivery.identity.dto.AddressRequest;
import com.delivery.identity.dto.CardDto;
import com.delivery.identity.dto.CardRequest;
import com.delivery.identity.dto.CustomerProfileDto;
import com.delivery.identity.dto.UpdateCustomerProfileRequest;
import com.delivery.identity.model.CustomerAddress;
import com.delivery.identity.model.CustomerCard;
import com.delivery.identity.model.Role;
import com.delivery.identity.model.User;
import com.delivery.identity.repository.CustomerAddressRepository;
import com.delivery.identity.repository.CustomerCardRepository;
import com.delivery.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.YearMonth;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Perfil do cliente final: dados pessoais, preferencias alimentares, enderecos e cartoes.
 * O numero completo do cartao nunca chega ao backend; so bandeira, ultimos 4 digitos e validade.
 */
@Service
@RequiredArgsConstructor
public class CustomerProfileService {

    private static final Set<String> SUPPORTED_PREFERENCES = Set.of(
            "VEGETARIAN", "VEGAN", "GLUTEN_FREE", "LACTOSE_FREE");
    private static final int MAX_ADDRESSES = 20;
    private static final int MAX_CARDS = 10;

    private final UserRepository userRepository;
    private final CustomerAddressRepository addressRepository;
    private final CustomerCardRepository cardRepository;

    public CustomerProfileDto getProfile(UUID userId) {
        return CustomerProfileDto.fromEntity(requireCustomer(userId));
    }

    @Transactional
    public CustomerProfileDto updateProfile(UUID userId, UpdateCustomerProfileRequest request) {
        User user = requireCustomer(userId);
        user.setName(request.getName().trim());
        user.setPhone(trimToNull(request.getPhone()));
        user.setFoodPreferences(normalizePreferences(request.getFoodPreferences()));
        return CustomerProfileDto.fromEntity(userRepository.save(user));
    }

    public List<AddressDto> listAddresses(UUID userId) {
        requireCustomer(userId);
        return addressRepository.findByUserIdOrderByCreatedAtAsc(userId).stream()
                .map(AddressDto::fromEntity)
                .toList();
    }

    @Transactional
    public AddressDto createAddress(UUID userId, AddressRequest request) {
        requireCustomer(userId);
        if (addressRepository.countByUserId(userId) >= MAX_ADDRESSES) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Voce ja cadastrou o limite de " + MAX_ADDRESSES + " enderecos");
        }
        boolean makeDefault = Boolean.TRUE.equals(request.getIsDefault())
                || addressRepository.countByUserId(userId) == 0;
        if (makeDefault) {
            clearDefaultAddress(userId);
        }
        CustomerAddress address = CustomerAddress.builder()
                .userId(userId)
                .label(request.getLabel().trim())
                .street(request.getStreet().trim())
                .number(request.getNumber().trim())
                .complement(trimToNull(request.getComplement()))
                .district(trimToNull(request.getDistrict()))
                .city(request.getCity().trim())
                .state(request.getState().trim().toUpperCase(Locale.ROOT))
                .zipCode(normalizeZip(request.getZipCode()))
                .reference(trimToNull(request.getReference()))
                .isDefault(makeDefault)
                .build();
        return AddressDto.fromEntity(addressRepository.save(address));
    }

    @Transactional
    public AddressDto updateAddress(UUID userId, UUID addressId, AddressRequest request) {
        requireCustomer(userId);
        CustomerAddress address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Endereco nao encontrado"));
        boolean makeDefault = Boolean.TRUE.equals(request.getIsDefault());
        if (makeDefault && !address.isDefault()) {
            clearDefaultAddress(userId);
        }
        address.setLabel(request.getLabel().trim());
        address.setStreet(request.getStreet().trim());
        address.setNumber(request.getNumber().trim());
        address.setComplement(trimToNull(request.getComplement()));
        address.setDistrict(trimToNull(request.getDistrict()));
        address.setCity(request.getCity().trim());
        address.setState(request.getState().trim().toUpperCase(Locale.ROOT));
        address.setZipCode(normalizeZip(request.getZipCode()));
        address.setReference(trimToNull(request.getReference()));
        address.setDefault(makeDefault || address.isDefault());
        return AddressDto.fromEntity(addressRepository.save(address));
    }

    @Transactional
    public void deleteAddress(UUID userId, UUID addressId) {
        requireCustomer(userId);
        CustomerAddress address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Endereco nao encontrado"));
        boolean wasDefault = address.isDefault();
        addressRepository.delete(address);
        if (wasDefault) {
            promoteFirstAddress(userId);
        }
    }

    @Transactional
    public AddressDto setDefaultAddress(UUID userId, UUID addressId) {
        requireCustomer(userId);
        CustomerAddress address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Endereco nao encontrado"));
        clearDefaultAddress(userId);
        address.setDefault(true);
        return AddressDto.fromEntity(addressRepository.save(address));
    }

    public List<CardDto> listCards(UUID userId) {
        requireCustomer(userId);
        return cardRepository.findByUserIdOrderByCreatedAtAsc(userId).stream()
                .map(CardDto::fromEntity)
                .toList();
    }

    @Transactional
    public CardDto createCard(UUID userId, CardRequest request) {
        requireCustomer(userId);
        if (cardRepository.countByUserId(userId) >= MAX_CARDS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Voce ja cadastrou o limite de " + MAX_CARDS + " cartoes");
        }
        validateExpiry(request.getExpiryMonth(), request.getExpiryYear());
        boolean makeDefault = Boolean.TRUE.equals(request.getIsDefault())
                || cardRepository.countByUserId(userId) == 0;
        if (makeDefault) {
            clearDefaultCard(userId);
        }
        CustomerCard card = CustomerCard.builder()
                .userId(userId)
                .brand(request.getBrand().trim())
                .last4(request.getLast4())
                .holderName(request.getHolderName().trim().toUpperCase(Locale.ROOT))
                .expiryMonth(request.getExpiryMonth())
                .expiryYear(request.getExpiryYear())
                .isDefault(makeDefault)
                .build();
        return CardDto.fromEntity(cardRepository.save(card));
    }

    @Transactional
    public CardDto setDefaultCard(UUID userId, UUID cardId) {
        requireCustomer(userId);
        CustomerCard card = cardRepository.findByIdAndUserId(cardId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cartao nao encontrado"));
        clearDefaultCard(userId);
        card.setDefault(true);
        return CardDto.fromEntity(cardRepository.save(card));
    }

    @Transactional
    public void deleteCard(UUID userId, UUID cardId) {
        requireCustomer(userId);
        CustomerCard card = cardRepository.findByIdAndUserId(cardId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cartao nao encontrado"));
        boolean wasDefault = card.isDefault();
        cardRepository.delete(card);
        if (wasDefault) {
            List<CustomerCard> remaining = cardRepository.findByUserIdOrderByCreatedAtAsc(userId);
            if (!remaining.isEmpty()) {
                CustomerCard next = remaining.get(0);
                next.setDefault(true);
                cardRepository.save(next);
            }
        }
    }

    private User requireCustomer(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conta nao encontrada"));
        if (user.getRole() != Role.CUSTOMER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Este perfil e exclusivo de contas de cliente");
        }
        if (!user.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Esta conta esta desativada");
        }
        return user;
    }

    private void clearDefaultAddress(UUID userId) {
        List<CustomerAddress> addresses = addressRepository.findByUserIdOrderByCreatedAtAsc(userId);
        addresses.forEach(item -> item.setDefault(false));
        addressRepository.saveAll(addresses);
    }

    private void promoteFirstAddress(UUID userId) {
        List<CustomerAddress> remaining = addressRepository.findByUserIdOrderByCreatedAtAsc(userId);
        if (!remaining.isEmpty()) {
            CustomerAddress first = remaining.get(0);
            first.setDefault(true);
            addressRepository.save(first);
        }
    }

    private void clearDefaultCard(UUID userId) {
        List<CustomerCard> cards = cardRepository.findByUserIdOrderByCreatedAtAsc(userId);
        cards.forEach(item -> item.setDefault(false));
        cardRepository.saveAll(cards);
    }

    private String normalizePreferences(List<String> preferences) {
        if (preferences == null || preferences.isEmpty()) {
            return null;
        }
        List<String> normalized = preferences.stream()
                .filter(item -> item != null && !item.isBlank())
                .map(item -> item.trim().toUpperCase(Locale.ROOT))
                .distinct()
                .toList();
        for (String preference : normalized) {
            if (!SUPPORTED_PREFERENCES.contains(preference)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Preferencia alimentar desconhecida: " + preference);
            }
        }
        return normalized.isEmpty() ? null : String.join(",", normalized);
    }

    private void validateExpiry(Integer month, Integer year) {
        YearMonth expiry = YearMonth.of(year, month);
        if (expiry.isBefore(YearMonth.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Este cartao ja esta vencido");
        }
    }

    private String normalizeZip(String zipCode) {
        if (zipCode == null || zipCode.isBlank()) {
            return null;
        }
        String digits = zipCode.replaceAll("\\D", "");
        if (digits.length() == 8) {
            return digits.substring(0, 5) + "-" + digits.substring(5);
        }
        return zipCode.trim();
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

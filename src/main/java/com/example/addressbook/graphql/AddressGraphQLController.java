package com.example.addressbook.graphql;

import com.example.addressbook.dto.AddressRequestDTO;
import com.example.addressbook.model.AddressEntry;
import com.example.addressbook.service.AddressService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class AddressGraphQLController {
  private final AddressService addressService;

  @QueryMapping
  public List<AddressEntry> addresses() {
    return addressService.listAll();
  }

  @QueryMapping
  public List<AddressEntry> addressesByUser(@Argument("userId") Long userId) {
    return addressService.listForUser(userId);
  }

  @MutationMapping
  public AddressEntry createAddress(@Argument("userId") Long userId, @Argument("input") AddressInput input) {
    AddressRequestDTO dto = new AddressRequestDTO(
        input.name(), input.phone(), input.email(), input.street());
    return addressService.createForUser(userId, dto);
  }

  @MutationMapping
  public Boolean deleteAddress(@Argument("id") Long id) {
    addressService.delete(id);
    return true;
  }

  public record AddressInput(String name, String phone, String email, String street) {}
}
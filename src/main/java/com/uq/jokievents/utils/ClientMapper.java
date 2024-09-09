package com.uq.jokievents.utils;

import com.uq.jokievents.dtos.RegisterClientDTO;
import com.uq.jokievents.dtos.UpdateClientDTO;
import com.uq.jokievents.model.Client;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ClientMapper {
    ClientMapper INSTANCE = Mappers.getMapper(ClientMapper.class);

    // Cliente a RegistrarCliente
    RegisterClientDTO  ClientToRegisterClientDTO(Client client);
    // RegistrarCliente a Cliente
    Client ClientRegisterDTOtoClient(RegisterClientDTO dto);

    UpdateClientDTO ClientToUpdateClientDTO(Client client);
    Client UpdateClientDTOtoClient(UpdateClientDTO dto);
}

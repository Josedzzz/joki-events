package com.uq.jokievents.utils;

import com.uq.jokievents.model.Client;
import com.uq.jokievents.records.RegisterClientDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ClientMapper {
    ClientMapper INSTANCE = Mappers.getMapper(ClientMapper.class);

    // Cliente a DTO
    RegisterClientDTO  ClientToRegisterClientDTO(Client client);
    // DTO a Cliente
    Client ClientRegisterDTOtoClient(RegisterClientDTO dto);
}

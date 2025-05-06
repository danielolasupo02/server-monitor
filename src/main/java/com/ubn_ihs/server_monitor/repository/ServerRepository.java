package com.ubn_ihs.server_monitor.repository;

import com.ubn_ihs.server_monitor.model.Server;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServerRepository extends JpaRepository<Server, Long> {

    List<Server> findByActiveTrue();

    Server findByServerIp(String serverIp);
}

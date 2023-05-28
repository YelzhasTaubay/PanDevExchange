package pan.dev.PanDevExchange.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pan.dev.PanDevExchange.entity.Messages;

@Repository
public interface MessageRepository extends JpaRepository<Messages,Long> {



}

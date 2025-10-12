package pro.sky.bankrecomendation.repository.dynamic;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.sky.bankrecomendation.model.dynamic.DynamicRule;

import java.util.UUID;

@Repository
public interface DynamicRuleRepository extends JpaRepository<DynamicRule, UUID> {
}
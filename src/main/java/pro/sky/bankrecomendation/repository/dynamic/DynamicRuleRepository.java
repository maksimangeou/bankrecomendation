package pro.sky.bankrecomendation.repository.dynamic;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pro.sky.bankrecomendation.model.dynamic.DynamicRule;

import java.util.List;
import java.util.UUID;

@Repository
public interface DynamicRuleRepository extends CrudRepository<DynamicRule, UUID> {

    @Query("SELECT * FROM dynamic_rules")
    List<DynamicRule> findAll();

    @Query("SELECT COUNT(*) > 0 FROM dynamic_rules WHERE product_id = :productId")
    boolean existsByProductId(@Param("productId") UUID productId);

    @Query("DELETE FROM dynamic_rules WHERE product_id = :productId")
    void deleteByProductId(@Param("productId") UUID productId);
}
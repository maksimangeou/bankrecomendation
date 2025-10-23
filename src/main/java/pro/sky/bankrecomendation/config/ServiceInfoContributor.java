package pro.sky.bankrecomendation.config;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ServiceInfoContributor implements InfoContributor {

    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> serviceInfo = new HashMap<>();
        serviceInfo.put("name", "Bank Recommendation Service");
        serviceInfo.put("version", getClass().getPackage().getImplementationVersion() != null ?
                getClass().getPackage().getImplementationVersion() : "1.0.0");

        builder.withDetail("service", serviceInfo);
    }
}
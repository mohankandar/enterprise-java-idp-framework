package io.github.mohankandar.idp.partner.registry;

import io.github.mohankandar.idp.partner.config.IdpPartnerProperties;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * Safe access to configured partners.
 */
public class PartnerServiceRegistry {

    private final Map<String, IdpPartnerProperties.PartnerServiceProperties> services;

    public PartnerServiceRegistry(IdpPartnerProperties props) {
        this.services = (props != null && props.getServices() != null) ? props.getServices() : Map.of();
    }

    public IdpPartnerProperties.PartnerServiceProperties get(String partnerName) {
        IdpPartnerProperties.PartnerServiceProperties p = services.get(partnerName);
        Assert.notNull(p, "Partner '" + partnerName + "' is not configured under idp.partners.services");
        return p;
    }

    public IdpPartnerProperties.RestProperties rest(String partnerName) {
        IdpPartnerProperties.PartnerServiceProperties p = get(partnerName);
        Assert.state(p.getType() == IdpPartnerProperties.PartnerType.REST,
            "Partner '" + partnerName + "' is not REST (type=" + p.getType() + ")");
        Assert.notNull(p.getRest(), "Partner '" + partnerName + "' REST config is missing");
        return p.getRest();
    }

    public IdpPartnerProperties.SoapProperties soap(String partnerName) {
        IdpPartnerProperties.PartnerServiceProperties p = get(partnerName);
        Assert.state(p.getType() == IdpPartnerProperties.PartnerType.SOAP,
            "Partner '" + partnerName + "' is not SOAP (type=" + p.getType() + ")");
        Assert.notNull(p.getSoap(), "Partner '" + partnerName + "' SOAP config is missing");
        return p.getSoap();
    }
    public boolean has(String name) {
        return name != null
                && !name.isBlank()
                && services.containsKey(name);
    }
}

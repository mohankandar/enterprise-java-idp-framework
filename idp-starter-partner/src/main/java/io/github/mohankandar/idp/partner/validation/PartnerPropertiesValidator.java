package io.github.mohankandar.idp.partner.validation;

import io.github.mohankandar.idp.partner.config.IdpPartnerProperties;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Fail-fast validation for partner configuration.
 */
public class PartnerPropertiesValidator implements SmartInitializingSingleton {

    private final IdpPartnerProperties props;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Framework validator intentionally holds a reference to configuration properties for startup validation."
    )
    public PartnerPropertiesValidator(IdpPartnerProperties props) {
        this.props = props;
    }

    @Override
    public void afterSingletonsInstantiated() {
        Map<String, IdpPartnerProperties.PartnerServiceProperties> services =
            (props != null && props.getServices() != null) ? props.getServices() : Map.of();

        List<String> errors = new ArrayList<>();

        for (Map.Entry<String, IdpPartnerProperties.PartnerServiceProperties> e : services.entrySet()) {
            String name = e.getKey();
            IdpPartnerProperties.PartnerServiceProperties svc = e.getValue();

            if (svc == null) {
                errors.add("Partner '" + name + "': config block is null");
                continue;
            }
            if (svc.getType() == null) {
                errors.add("Partner '" + name + "': type is required (REST|SOAP)");
                continue;
            }

            if (svc.getType() == IdpPartnerProperties.PartnerType.REST) {
                IdpPartnerProperties.RestProperties r = svc.getRest();
                if (r == null) {
                    errors.add("Partner '" + name + "': rest section is required for type=REST");
                } else {
                    if (!StringUtils.hasText(r.getBaseUrl())) {
                        errors.add("Partner '" + name + "': rest.baseUrl is required");
                    }
                    if (r.getAuth() != null && r.getAuth().getMode() == IdpPartnerProperties.RestAuthMode.API_KEY) {
                        if (!StringUtils.hasText(r.getAuth().getApiKeyHeader())) {
                            errors.add("Partner '" + name + "': rest.auth.apiKeyHeader is required when auth.mode=API_KEY");
                        }
                        if (!StringUtils.hasText(r.getAuth().getApiKeyValue())) {
                            errors.add("Partner '" + name + "': rest.auth.apiKeyValue is required when auth.mode=API_KEY");
                        }
                    }
                    if (r.getAuth() != null && r.getAuth().getMode() == IdpPartnerProperties.RestAuthMode.OAUTH) {
                        IdpPartnerProperties.OAuthClientCredentialsProperties oauth = r.getAuth().resolveOauthClientCredentials();
                        if (oauth == null || !StringUtils.hasText(oauth.resolveTokenUrl())) {
                            errors.add("Partner '" + name + "': rest.auth.oauthClientCredentials.tokenUrl (or legacy rest.auth.oauth.accessTokenUrl) is required when auth.mode=OAUTH");
                        }
                        if (oauth == null || !StringUtils.hasText(oauth.getClientId())) {
                            errors.add("Partner '" + name + "': rest.auth.oauthClientCredentials.clientId is required when auth.mode=OAUTH");
                        }
                        if (oauth == null || !StringUtils.hasText(oauth.getClientSecret())) {
                            errors.add("Partner '" + name + "': rest.auth.oauthClientCredentials.clientSecret is required when auth.mode=OAUTH");
                        }
                    }
                }
            } else if (svc.getType() == IdpPartnerProperties.PartnerType.SOAP) {
                IdpPartnerProperties.SoapProperties s = svc.getSoap();
                if (s == null) {
                    errors.add("Partner '" + name + "': soap section is required for type=SOAP");
                } else {
                    if (!StringUtils.hasText(s.getEndpoint())) {
                        errors.add("Partner '" + name + "': soap.endpoint is required");
                    }
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new IllegalStateException("Invalid IDP partner configuration:\n - " + String.join("\n - ", errors));
        }
    }
}

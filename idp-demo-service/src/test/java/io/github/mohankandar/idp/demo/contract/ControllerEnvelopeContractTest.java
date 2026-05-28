package io.github.mohankandar.idp.demo.contract;

import io.github.mohankandar.idp.core.api.ApiResponse;
import io.github.mohankandar.idp.demo.controller.CustomerController;
import io.github.mohankandar.idp.demo.controller.DownstreamController;
import io.github.mohankandar.idp.demo.controller.EchoController;
import io.github.mohankandar.idp.demo.controller.ExternalEchoController;
import io.github.mohankandar.idp.demo.controller.MeController;
import io.github.mohankandar.idp.demo.controller.PingController;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ControllerEnvelopeContractTest {

    private static final List<Class<?>> CONTROLLERS = List.of(
            PingController.class,
            MeController.class,
            CustomerController.class,
            DownstreamController.class,
            ExternalEchoController.class,
            EchoController.class
    );

    @Test
    void requestHandlersUseIdpEnvelopeOrExplicitNoContentResponse() {
        for (Class<?> controller : CONTROLLERS) {
            assertThat(controller.isAnnotationPresent(RestController.class))
                    .as("%s must be a REST controller", controller.getSimpleName())
                    .isTrue();

            for (Method method : controller.getDeclaredMethods()) {
                if (!isRequestHandler(method)) {
                    continue;
                }
                assertThat(isAllowedReturnType(method))
                        .as("%s#%s must return ApiResponse or ResponseEntity<ApiResponse> (204 delete may return ResponseEntity<Void>)",
                                controller.getSimpleName(), method.getName())
                        .isTrue();
            }
        }
    }

    private static boolean isRequestHandler(Method method) {
        return method.isAnnotationPresent(GetMapping.class)
                || method.isAnnotationPresent(PostMapping.class)
                || method.isAnnotationPresent(PutMapping.class)
                || method.isAnnotationPresent(DeleteMapping.class)
                || method.isAnnotationPresent(PatchMapping.class);
    }

    private static boolean isAllowedReturnType(Method method) {
        Class<?> rawType = method.getReturnType();
        if (ApiResponse.class.equals(rawType)) {
            return true;
        }
        if (!ResponseEntity.class.equals(rawType)) {
            return false;
        }

        Type generic = method.getGenericReturnType();
        if (!(generic instanceof ParameterizedType parameterizedType)) {
            return false;
        }
        Type actual = parameterizedType.getActualTypeArguments()[0];
        if (actual instanceof Class<?> actualClass) {
            return Void.class.equals(actualClass);
        }
        if (actual instanceof ParameterizedType nestedType) {
            return ApiResponse.class.equals(nestedType.getRawType());
        }
        return false;
    }
}

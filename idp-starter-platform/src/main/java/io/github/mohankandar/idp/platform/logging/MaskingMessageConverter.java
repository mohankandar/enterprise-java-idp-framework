package io.github.mohankandar.idp.platform.logging;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import io.github.mohankandar.idp.core.util.MaskingUtil;

public class MaskingMessageConverter extends ClassicConverter {
    @Override
    public String convert(ILoggingEvent event) {
        String msg = event.getFormattedMessage(); // includes params already applied
        return MaskingUtil.maskAll(msg);
    }
}

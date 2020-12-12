package com.sap.hcp.cf.logging.servlet.filter;

import org.slf4j.MDC;

/**
 * <p>
 * THe {@link RequestLoggingFilter} extracts information from HTTP requests and
 * can create request logs. It will read several HTTP Headers and store them in
 * the SLF4J MDC, so that all log messages created during request handling will
 * have those additional fields. It will also instrument the request to generate
 * a request log containing metrics such as request and response sizes and
 * response time. This instrumentation can be disabled by denying logs from
 * {@link RequestLogger} with marker "request".
 * </p>
 * <p>
 * This filter will generate a correlation id, from the HTTP header
 * "X-CorrelationID" falling back to "x-vcap-request-id" if not found or using a
 * random UUID. The correlation id will be added as an HTTP header
 * "X-CorrelationID" to the response if possible.
 * </p>
 * <p>
 * This filter supports dynamic log levels activated by JWT tokens in HTTP
 * headers. Setup and processing of these tokens can be changed with own
 * implementations of {@link DynamicLogLevelFilter}.
 * </p>
 * <p>
 * To use the filter, it needs to be added to the servlet configuration. It has
 * a default constructor to support web.xml configuration. You can customize the
 * filter by creating your own subclass of {@link CompositeFilter} and mix and
 * match any of the provided filters and add your own implementation:
 * <ul>
 * <li>{@link AddVcapEnvironmentToLogContextFilter} provide application metadata
 * (app_id, app_name, ...) from environment</li>
 * <li>{@link AddHttpHeadersToLogContextFilter} provides certain HTTP headers to
 * {@link MDC}</li>
 * <li>{@link CorrelationIdFilter} extracts "X-CorrelationId" HTTP header or
 * generates new to add to {@link MDC}</li>
 * <li>{@link DynamicLogLevelFilter} supports JWT based dynamic changes of log
 * level per request</li>
 * <li>{@link GenerateRequestLogFilter} instruments the request to generate the
 * final request log</li>
 * </ul>
 * </p>
 */
public class RequestLoggingFilter extends CompositeFilter {

    public RequestLoggingFilter() {
        super(new AddVcapEnvironmentToLogContextFilter(), new AddHttpHeadersToLogContextFilter(),
              new CorrelationIdFilter(), new DynamicLogLevelFilter(), new GenerateRequestLogFilter());
    }

    public RequestLoggingFilter(RequestRecordFactory requestRecordFactory) {
        super(new AddVcapEnvironmentToLogContextFilter(), new AddHttpHeadersToLogContextFilter(),
              new CorrelationIdFilter(), new DynamicLogLevelFilter(), new GenerateRequestLogFilter(
                                                                                                   requestRecordFactory));
    }
}

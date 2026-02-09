package pl.election.config;

public final class SecurityHeaders {

    public static final String X_CONTENT_TYPE_OPTIONS = "X-Content-Type-Options";
    public static final String X_FRAME_OPTIONS = "X-Frame-Options";
    public static final String CONTENT_SECURITY_POLICY = "Content-Security-Policy";
    public static final String STRICT_TRANSPORT_SECURITY = "Strict-Transport-Security";

    public static final String VALUE_NOSNIFF = "nosniff";
    public static final String VALUE_DENY = "DENY";
    public static final String VALUE_CSP = "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data:";
    public static final String VALUE_HSTS = "max-age=31536000; includeSubDomains";

    private SecurityHeaders() {
    }
}

package br.com.issler.fake_gateway.security;

import br.com.issler.fake_gateway.exceptions.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class IdempotencyKeyFilter extends OncePerRequestFilter {
    private final ObjectMapper mapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String idempotencyKey = request.getHeader("Idempotency-Key");

        if (!StringUtils.hasText(idempotencyKey)) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            ErrorResponse error = ErrorResponse.builder()
                    .error("MISSING_IDEMPOTENCY_KEY")
                    .message("Idempotency key is missing.")
                    .status(HttpStatus.BAD_REQUEST.value())
                    .build();

            response.getWriter().write(mapper.writeValueAsString(error));
            return;
        }

        filterChain.doFilter(request, response);
    }
}

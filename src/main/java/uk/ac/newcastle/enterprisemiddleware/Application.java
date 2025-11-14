package uk.ac.newcastle.enterprisemiddleware;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;

import javax.ws.rs.ApplicationPath;

// Main application class for JAX-RS
@ApplicationPath("/api")
@OpenAPIDefinition(
    info = @Info(
        title = "Hotel Booking Service API",
        version = "1.0.0",
        description = "REST API for hotel bookings and travel agent services",
        contact = @Contact(
            name = "Shantanu Raj Chaudhary",
            email = "shantanu.raj@example.com"
        )
    )
)
public class Application extends javax.ws.rs.core.Application {
    // no additional config needed for now
}

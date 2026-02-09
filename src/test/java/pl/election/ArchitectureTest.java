package pl.election;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@DisplayName("Hexagonal Architecture Rules")
class ArchitectureTest {

    private static final String BASE_PACKAGE = "pl.election";
    private static final String DOMAIN = "..domain..";
    private static final String APPLICATION = "..application..";
    private static final String ADAPTER_IN = "..adapter.in..";
    private static final String ADAPTER_OUT = "..adapter.out..";
    private static final String CONFIG = "..config..";

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(BASE_PACKAGE);
    }

    @Nested
    @DisplayName("Domain layer isolation")
    class DomainLayerRules {

        @Test
        @DisplayName("Domain should not depend on Spring framework")
        void should_notDependOnSpring_when_inDomainLayer() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(DOMAIN)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "org.springframework..",
                            "jakarta.persistence..",
                            "jakarta.transaction.."
                    )
                    .because("Domain layer must be framework-free (hexagonal architecture)");

            rule.check(classes);
        }

        @Test
        @DisplayName("Domain should not depend on adapters")
        void should_notDependOnAdapters_when_inDomainLayer() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(DOMAIN)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(ADAPTER_IN, ADAPTER_OUT)
                    .because("Domain must not know about adapters (dependency inversion)");

            rule.check(classes);
        }

        @Test
        @DisplayName("Domain should not depend on application layer")
        void should_notDependOnApplication_when_inDomainLayer() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(DOMAIN)
                    .should().dependOnClassesThat()
                    .resideInAPackage(APPLICATION)
                    .because("Domain is the innermost layer and must not depend on application");

            rule.check(classes);
        }

        @Test
        @DisplayName("Domain should not depend on config")
        void should_notDependOnConfig_when_inDomainLayer() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(DOMAIN)
                    .should().dependOnClassesThat()
                    .resideInAPackage(CONFIG)
                    .because("Domain must not depend on infrastructure configuration");

            rule.check(classes);
        }
    }

    @Nested
    @DisplayName("Application layer isolation")
    class ApplicationLayerRules {

        @Test
        @DisplayName("Application should not depend on Spring framework")
        void should_notDependOnSpring_when_inApplicationLayer() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(APPLICATION)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "org.springframework..",
                            "jakarta.persistence..",
                            "jakarta.transaction.."
                    )
                    .because("Application layer must be framework-free (hexagonal architecture)");

            rule.check(classes);
        }

        @Test
        @DisplayName("Application should not depend on adapters")
        void should_notDependOnAdapters_when_inApplicationLayer() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(APPLICATION)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(ADAPTER_IN, ADAPTER_OUT)
                    .because("Application must not know about adapter implementations");

            rule.check(classes);
        }

        @Test
        @DisplayName("Application should not depend on config")
        void should_notDependOnConfig_when_inApplicationLayer() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(APPLICATION)
                    .should().dependOnClassesThat()
                    .resideInAPackage(CONFIG)
                    .because("Application must not depend on infrastructure configuration");

            rule.check(classes);
        }

        @Test
        @DisplayName("Application may only depend on domain")
        void should_onlyDependOnDomain_when_inApplicationLayer() {
            ArchRule rule = classes()
                    .that().resideInAPackage(APPLICATION)
                    .should().onlyDependOnClassesThat()
                    .resideInAnyPackage(
                            APPLICATION, DOMAIN,
                            "java..",
                            "lombok.."
                    )
                    .because("Application layer depends only on domain and standard Java");

            rule.check(classes);
        }
    }

    @Nested
    @DisplayName("Adapter layer rules")
    class AdapterLayerRules {

        @Test
        @DisplayName("Input adapters should not depend on output adapters")
        void should_notDependOnOutputAdapters_when_inInputAdapters() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(ADAPTER_IN)
                    .should().dependOnClassesThat()
                    .resideInAPackage(ADAPTER_OUT)
                    .because("Input and output adapters must be independent");

            rule.check(classes);
        }

        @Test
        @DisplayName("Output adapters should not depend on input adapters")
        void should_notDependOnInputAdapters_when_inOutputAdapters() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(ADAPTER_OUT)
                    .should().dependOnClassesThat()
                    .resideInAPackage(ADAPTER_IN)
                    .because("Input and output adapters must be independent");

            rule.check(classes);
        }

        @Test
        @DisplayName("Adapters should not depend on config")
        void should_notDependOnConfig_when_inAdapters() {
            ArchRule rule = noClasses()
                    .that().resideInAnyPackage(ADAPTER_IN, ADAPTER_OUT)
                    .should().dependOnClassesThat()
                    .resideInAPackage(CONFIG)
                    .because("Adapters must not depend on Spring configuration classes");

            rule.check(classes);
        }
    }

    @Nested
    @DisplayName("Port rules")
    class PortRules {

        @Test
        @DisplayName("Input ports should be interfaces")
        void should_beInterfaces_when_inInputPorts() {
            ArchRule rule = classes()
                    .that().resideInAPackage("..application.port.in..")
                    .and().haveSimpleNameEndingWith("UseCase")
                    .should().beInterfaces()
                    .because("Input ports (use cases) define contracts as interfaces");

            rule.check(classes);
        }

        @Test
        @DisplayName("Output ports should be interfaces")
        void should_beInterfaces_when_inOutputPorts() {
            ArchRule rule = classes()
                    .that().resideInAPackage("..application.port.out..")
                    .and().haveSimpleNameEndingWith("Repository")
                    .should().beInterfaces()
                    .because("Output ports (repositories) define contracts as interfaces");

            rule.check(classes);
        }
    }

    @Nested
    @DisplayName("Dependency direction")
    class DependencyDirectionRules {

        @Test
        @DisplayName("No cyclic dependencies between slices")
        void should_haveNoCycles_when_checkingPackageDependencies() {
            ArchRule rule = slices()
                    .matching("pl.election.(*)..")
                    .should().beFreeOfCycles()
                    .because("Hexagonal architecture requires acyclic dependency graph");

            rule.check(classes);
        }
    }

    @Nested
    @DisplayName("Naming conventions")
    class NamingConventionRules {

        @Test
        @DisplayName("Controllers should reside in adapter.in.web package")
        void should_resideInWebPackage_when_namedController() {
            ArchRule rule = classes()
                    .that().haveSimpleNameEndingWith("Controller")
                    .should().resideInAPackage("..adapter.in.web..")
                    .because("Controllers are input adapters and belong in adapter.in.web");

            rule.check(classes);
        }

        @Test
        @DisplayName("JPA entities should reside in adapter.out.persistence package")
        void should_resideInPersistencePackage_when_namedEntity() {
            ArchRule rule = classes()
                    .that().haveSimpleNameEndingWith("Entity")
                    .should().resideInAPackage("..adapter.out.persistence..")
                    .because("JPA entities are persistence concerns and belong in adapter.out");

            rule.check(classes);
        }

        @Test
        @DisplayName("Services should reside in application.service package")
        void should_resideInServicePackage_when_namedService() {
            ArchRule rule = classes()
                    .that().haveSimpleNameEndingWith("Service")
                    .and().resideInAPackage("..application..")
                    .should().resideInAPackage("..application.service..")
                    .because("Application services implement use cases in application.service");

            rule.check(classes);
        }
    }
}

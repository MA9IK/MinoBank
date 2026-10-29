package com.aiven.minobank;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModulithTests {


    @Test
    void verifyModulithStructure() {
        ApplicationModules.of(MinoBankApplication.class).verify();
    }
}

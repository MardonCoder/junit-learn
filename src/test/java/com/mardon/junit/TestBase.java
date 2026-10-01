package com.mardon.junit;

import com.mardon.junit.extension.GlobalExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({
        GlobalExtension.class
})
public abstract class TestBase {
}

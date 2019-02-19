package org.hisp.dhis.android.testapp.configuration;

import android.support.test.runner.AndroidJUnit4;

import org.hisp.dhis.android.core.configuration.Configuration;
import org.hisp.dhis.android.testapp.arch.BasePublicAccessShould;
import org.junit.runner.RunWith;
import org.mockito.Mock;

@RunWith(AndroidJUnit4.class)
public class ConfigurationPublicAccessShould extends BasePublicAccessShould<Configuration> {

    @Mock
    private Configuration object;

    @Override
    public Configuration object() {
        return object;
    }

    @Override
    public void has_public_create_method() {
        Configuration.create(null);
    }

    @Override
    public void has_public_builder_method() {
        Configuration.builder();
    }

    @Override
    public void has_public_to_builder_method() {
        object().toBuilder();
    }
}
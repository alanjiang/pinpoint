/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.test;

import com.google.inject.Inject;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.profiler.context.ApplicationContext;
import com.navercorp.pinpoint.profiler.instrument.ClassInjector;
import com.navercorp.pinpoint.profiler.plugin.ClassFileTransformerLoader;
import com.navercorp.pinpoint.profiler.plugin.PluginInstrumentContext;
import com.navercorp.pinpoint.profiler.plugin.DefaultProfilerPluginSetupContext;
import com.navercorp.pinpoint.profiler.plugin.GuardProfilerPluginContext;
import com.navercorp.pinpoint.profiler.plugin.PluginSetup;
import com.navercorp.pinpoint.profiler.plugin.SetupResult;

/**
 * @author Woonduk Kang(emeroad)
 */
public class MockPluginSetup implements PluginSetup {


    private final ProfilerConfig profilerConfig;
    private final ApplicationContext applicationContext;

    @Inject
    public MockPluginSetup(ProfilerConfig profilerConfig, ApplicationContext applicationContext) {
        this.profilerConfig = profilerConfig;

        this.applicationContext = applicationContext;
    }

    @Override
    public SetupResult setupPlugin(ProfilerPlugin plugin, ClassInjector classInjector) {

        final DefaultProfilerPluginSetupContext pluginSetupContext = new DefaultProfilerPluginSetupContext(profilerConfig);
        final GuardProfilerPluginContext guardPluginSetupContext = new GuardProfilerPluginContext(pluginSetupContext);

        ClassFileTransformerLoader classFileTransformerLoader = new ClassFileTransformerLoader(applicationContext);
        InstrumentContext instrumentContext = new PluginInstrumentContext(applicationContext, classInjector, classFileTransformerLoader);
        try {
            preparePlugin(plugin, instrumentContext);
            plugin.setup(guardPluginSetupContext);
        } finally {
            guardPluginSetupContext.close();
        }
        SetupResult setup = new SetupResult(pluginSetupContext, classFileTransformerLoader);
        return setup;
    }


    /**
     * TODO duplicated code : com/navercorp/pinpoint/profiler/plugin/ProfilerPluginLoader.java
     * @param plugin
     * @param context
     */
    private void preparePlugin(ProfilerPlugin plugin, InstrumentContext context) {

        if (plugin instanceof TransformTemplateAware) {
            final TransformTemplate transformTemplate = new TransformTemplate(context);
            ((TransformTemplateAware) plugin).setTransformTemplate(transformTemplate);
        }
    }
}
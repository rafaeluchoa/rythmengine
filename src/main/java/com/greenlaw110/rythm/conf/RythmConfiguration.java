/* 
 * Copyright (C) 2013 The Rythm Engine project
 * Gelin Luo <greenlaw110(at)gmail.com>
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package com.greenlaw110.rythm.conf;

import com.greenlaw110.rythm.Rythm;
import com.greenlaw110.rythm.extension.IByteCodeEnhancer;
import com.greenlaw110.rythm.extension.IByteCodeHelper;
import com.greenlaw110.rythm.extension.IDurationParser;
import com.greenlaw110.rythm.extension.ILang;
import com.greenlaw110.rythm.template.ITemplate;

import java.io.File;
import java.util.*;

import static com.greenlaw110.rythm.conf.RythmConfigurationKey.*;

/**
 * Store the configuration for a {@link com.greenlaw110.rythm.RythmEngine rythm engine}
 * instance
 */
public class RythmConfiguration {
    private Map<String, Object> raw;
    private Map<RythmConfigurationKey, Object> data;

    /**
     * Construct a <code>RythmConfiguration</code> with a map. The map is copied to
     * the original map of the configuration instance
     *
     * @param configuration
     */
    public RythmConfiguration(Map<String, ?> configuration) {
        raw = new HashMap<String, Object>(configuration);
        data = new HashMap<RythmConfigurationKey, Object>(configuration.size());
    }

    /**
     * Return configuration by {@link RythmConfigurationKey configuration key}
     *
     * @param key
     * @param <T>
     * @return
     */
    public <T> T get(RythmConfigurationKey key) {
        Object o = data.get(key);
        if (null == o) {
            o = key.getConfiguration(raw);
            if (null != o) {
                data.put(key, o);
            } else {
                data.put(key, ITemplate.RawData.NULL);
            }
        }
        if (o == ITemplate.RawData.NULL) {
            return null;
        } else {
            return (T) o;
        }
    }

    /**
     * Look up configuration by a <code>String<code/> key. If the String key
     * can be converted into {@link RythmConfigurationKey rythm configuration key}, then
     * it is converted and call to {@link #get(RythmConfigurationKey)} method. Otherwise
     * the original configuration map is used to fetch the value from the string key
     *
     * @param key
     * @param <T>
     * @return
     */
    public <T> T get(String key) {
        if (key.startsWith("rythm.")) {
            key = key.replaceFirst("rythm.", "");
        }
        RythmConfigurationKey rk = RythmConfigurationKey.valueOfIgnoreCase(key);
        if (null != rk) {
            return get(rk);
        } else {
            return (T) raw.get(key);
        }
    }

    // speed up access to frequently accessed and non-modifiable configuration items
    private String _pluginVersion = null;

    /**
     * Return {@link RythmConfigurationKey#ENGINE_PLUGIN_VERSION plugin version} without lookup
     *
     * @return
     */
    public String pluginVersion() {
        if (null == _pluginVersion) {
            _pluginVersion = get(ENGINE_PLUGIN_VERSION);
        }
        return _pluginVersion;
    }

    private IByteCodeHelper _byteCodeHelper = null;

    /**
     * Return {@link RythmConfigurationKey#ENGINE_CLASS_LOADER_BYTECODE_HELPER_IMPL} without lookup
     *
     * @return
     */
    public IByteCodeHelper byteCodeHelper() {
        if (null == _byteCodeHelper) {
            _byteCodeHelper = get(ENGINE_CLASS_LOADER_BYTECODE_HELPER_IMPL);
        }
        return _byteCodeHelper;
    }

    private Boolean _play = false;

    /**
     * Return {@link RythmConfigurationKey#ENGINE_PLAYFRAMEWORK} without lookup
     *
     * @return
     */
    public boolean playFramework() {
        if (null == _play) {
            _play = get(ENGINE_PLAYFRAMEWORK);
        }
        return _play;
    }

    private Boolean _logRenderTime = null;

    /**
     * Return {@link RythmConfigurationKey#LOG_TIME_RENDER_ENABLED} without
     * look up
     *
     * @return
     */
    public boolean logRenderTime() {
        if (null == _logRenderTime) {
            _logRenderTime = get(LOG_TIME_RENDER_ENABLED);
        }
        return _logRenderTime;
    }

    private Boolean _loadPrecompiled = null;

    /**
     * Return {@link RythmConfigurationKey#ENGINE_LOAD_PRECOMPILED_ENABLED}
     * without lookup
     *
     * @return
     */
    public boolean loadPrecompiled() {
        if (null == _loadPrecompiled) {
            _loadPrecompiled = get(ENGINE_LOAD_PRECOMPILED_ENABLED);
        }
        return _loadPrecompiled;
    }

    private Boolean _precompileMode = null;

    /**
     * Return {@link RythmConfigurationKey#ENGINE_PRECOMPILE_MODE} without lookup
     *
     * @return
     */
    public boolean precompileMode() {
        if (null == _precompileMode) {
            _precompileMode = get(ENGINE_PRECOMPILE_MODE);
        }
        return _precompileMode;
    }

    private Boolean _disableFileWrite = null;

    /**
     * Return inversed value of {@link RythmConfigurationKey#ENGINE_FILE_WRITE_ENABLED}
     * without lookup
     *
     * @return
     */
    public boolean disableFileWrite() {
        if (null == _disableFileWrite) {
            boolean b = (Boolean) get(ENGINE_FILE_WRITE_ENABLED);
            _disableFileWrite = !b;
        }
        return _disableFileWrite;
    }

    private Set<String> _restrictedClasses = null;

    /**
     * Return {@link RythmConfigurationKey#SANDBOX_RESTRICTED_CLASS} without lookup
     * <p/>
     * <p>Note, the return value also contains rythm's built-in restricted classes</p>
     *
     * @return
     */
    public Set<String> restrictedClasses() {
        if (null == _restrictedClasses) {
            String s = get(SANDBOX_RESTRICTED_CLASS);
            s += ";com.greenlaw110.rythm.Rythm;com.greenlaw110.rythm.RythmEngine;java.io;java.nio;java.security;java.rmi;java.net;java.awt;java.applet";
            _restrictedClasses = new HashSet<String>(Arrays.asList(s.split(";")));
        }
        return new HashSet<String>(_restrictedClasses);
    }

    private Boolean _enableTypeInference = null;

    /**
     * Get {@link RythmConfigurationKey#FEATURE_TYPE_INFERENCE_ENABLED} without
     * lookup
     *
     * @return
     */
    public boolean typeInferenceEnabled() {
        if (null == _enableTypeInference) {
            _enableTypeInference = get(FEATURE_TYPE_INFERENCE_ENABLED);
        }
        return _enableTypeInference;
    }

    private Boolean _smartEscapeEnabled = null;

    /**
     * Get {@link RythmConfigurationKey#FEATURE_SMART_ESCAPE_ENABLED} without lookup
     *
     * @return
     */
    public boolean smartEscapeEnabled() {
        if (null == _smartEscapeEnabled) {
            _smartEscapeEnabled = (Boolean)get(FEATURE_SMART_ESCAPE_ENABLED);
        }
        return _smartEscapeEnabled;
    }

    private Boolean _naturalTemplateEnabled = null;

    public boolean naturalTemplateEnabled() {
        if (null == _naturalTemplateEnabled) {
            _naturalTemplateEnabled = (Boolean)get(FEATURE_NATURAL_TEMPLATE_ENABLED);
        }
        return _naturalTemplateEnabled;
    }
    
    private Boolean _debugJavaSourceEnabled = null;
    
    public boolean debugJavaSourceEnabled() {
        if (null == _debugJavaSourceEnabled) {
            _debugJavaSourceEnabled = (Boolean)get(ENGINE_DEBUG_JAVA_SOURCE_ENABLED);
        }
        return _debugJavaSourceEnabled;
    }

    private Boolean _cacheEnabled = null;

    /**
     * Return true if cache is not disabled for the engine instance. A cache is disabled when
     * <ul>
     * <li>{@link RythmConfigurationKey#CACHE_ENABLED} is <code>true</code> or</li>
     * <li>{@link RythmConfigurationKey#CACHE_PROD_ONLY_ENABLED} is <code>true</code> and
     * {@link RythmConfigurationKey#ENGINE_MODE} is {@link Rythm.Mode#dev}</li>
     * </ul>
     *
     * @return
     */
    public boolean cacheEnabled() {
        if (null == _cacheEnabled) {
            boolean ce = (Boolean) get(CACHE_ENABLED);
            Rythm.Mode mode = get(ENGINE_MODE);
            boolean po = (Boolean) get(CACHE_PROD_ONLY_ENABLED);
            if (!ce) {
                _cacheEnabled = false;
            } else {
                if (mode.isDev() && po) {
                    _cacheEnabled = false;
                } else {
                    _cacheEnabled = true;
                }
            }
        }
        return _cacheEnabled;
    }

    /**
     * Return true if cache is disabled for the engine instance.
     *
     * @return
     * @see #cacheEnabled()
     */
    public boolean cacheDisabled() {
        return !cacheEnabled();
    }

    private Boolean _transformEnabled = null;

    /**
     * Return {@link RythmConfigurationKey#FEATURE_TRANSFORM_ENABLED} without look up
     *
     * @return
     */
    public boolean transformEnabled() {
        if (null == _transformEnabled) {
            _transformEnabled = get(FEATURE_TRANSFORM_ENABLED);
        }
        return _transformEnabled;
    }

    private Boolean _compactEnabled = null;


    public boolean compactModeEnabled() {
        if (null == _compactEnabled) {
            _compactEnabled = get(CODEGEN_COMPACT_ENABLED);
        }
        return _compactEnabled;
    }

    private IDurationParser _durationParser = null;

    /**
     * Return {@link RythmConfigurationKey#CACHE_DURATION_PARSER_IMPL} without lookup
     *
     * @return
     */
    public IDurationParser durationParser() {
        if (null == _durationParser) {
            _durationParser = get(CACHE_DURATION_PARSER_IMPL);
        }
        return _durationParser;
    }

    private ILang _defaultLang = null;

    /**
     * Return {@link RythmConfigurationKey#DEFAULT_TEMPLATE_LANG_IMPL} without lookup
     *
     * @return
     */
    public ILang defaultLang() {
        if (null == _defaultLang) {
            _defaultLang = get(DEFAULT_TEMPLATE_LANG_IMPL);
        }
        return _defaultLang;
    }

    private File _tmpDir = null;

    /**
     * Return {@link RythmConfigurationKey#HOME_TMP} without lookup
     *
     * @return
     */
    public File tmpDir() {
        if (null == _tmpDir) {
            _tmpDir = get(HOME_TMP);
        }
        return _tmpDir;
    }

    private File _templateHome = null;

    /**
     * Return {@link RythmConfigurationKey#HOME_TEMPLATE} without lookup
     *
     * @return
     */
    public File templateHome() {
        if (null == _templateHome) {
            _templateHome = get(RythmConfigurationKey.HOME_TEMPLATE);
        }
        return _templateHome;
    }

    /**
     * Set template source home path
     * <p/>
     * <p><b>Note</b>, this is not supposed to be used by user application or third party plugin</p>
     */
    public void setTemplateHome(File home) {
        raw.put(HOME_TEMPLATE.getKey(), home);
        data.put(HOME_TEMPLATE, home);
    }

    private IByteCodeEnhancer _byteCodeEnhancer = IByteCodeEnhancer.INSTS.NULL;

    public IByteCodeEnhancer byteCodeEnhancer() {
        if (IByteCodeEnhancer.INSTS.NULL == _byteCodeEnhancer) {
            _byteCodeEnhancer = get(CODEGEN_BYTE_CODE_ENHANCER);
        }
        return _byteCodeEnhancer;
    }

    private String _lang = null;

    /**
     * Get {@link RythmConfigurationKey#I18N_LANG} without lookup
     *
     * @return
     */
    public String lang() {
        if (null == _lang) {
            _lang = get(I18N_LANG);
        }
        return _lang;
    }

    private String _locale = null;

    /**
     * Get {@link RythmConfigurationKey#I18N_LOCALE} without lookup
     *
     * @return
     */
    public String locale() {
        if (null == _locale) {
            _locale = get(I18N_LOCALE);
        }
        return _locale;
    }
}
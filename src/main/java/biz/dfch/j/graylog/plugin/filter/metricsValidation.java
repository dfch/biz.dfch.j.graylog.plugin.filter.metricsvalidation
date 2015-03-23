package biz.dfch.j.graylog.plugin.filter;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.io.FilenameUtils;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.filters.*;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the plugin. Your class should implement one of the existing plugin
 * interfaces. (i.e. AlarmCallback, MessageInput, MessageOutput)
 */
public class metricsValidation implements MessageFilter
{
    private static final String DF_PLUGIN_NAME = "d-fens metricsValidation filter";
    private Map metrics = new HashMap();

    private static final String DF_PLUGIN_PRIORITY = "DF_PLUGIN_PRIORITY";
    private static final String DF_PLUGIN_DROP_MESSAGE = "DF_PLUGIN_DROP_MESSAGE";
    private static final String DF_PLUGIN_DISABLED = "DF_PLUGIN_DISABLED";
    private static final String DF_PLUGIN_METRICS = "DF_PLUGIN_METRICS";

    // for performance reasons these configuration items have internal variables
    // DF_PLUGIN_DISABLED, DF_PLUGIN_DROP_MESSAGE
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicBoolean dropMessage = new AtomicBoolean(false);
    private Configuration configuration;
    private String configurationFileName;

    private ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
    private ScriptEngine scriptEngine;
    private ScriptContext scriptContext;
    private File file;

    private static final Logger LOG = LoggerFactory.getLogger(metricsValidation.class);

    public metricsValidation() throws IOException, URISyntaxException
    {
        try
        {
            LOG.debug(String.format("[%d] Initialising plugin ...\r\n", Thread.currentThread().getId()));

            // get config file
            CodeSource codeSource = this.getClass().getProtectionDomain().getCodeSource();
            URI uri = codeSource.getLocation().toURI();

            // String path = uri.getSchemeSpecificPart();
            // path would contain absolute path including jar file name with extension
            // String path = FilenameUtils.getPath(uri.getPath());
            // path would contain relative path (no leading '/' and no jar file name

            String path = FilenameUtils.getPath(uri.getPath());
            if(!path.startsWith("/"))
            {
                path = String.format("/%s", path);
            }
            String baseName = FilenameUtils.getBaseName(uri.getSchemeSpecificPart());
            if(null == baseName || baseName.isEmpty())
            {
                baseName = this.getClass().getPackage().getName();
            }

            // get config values
            configurationFileName = FilenameUtils.concat(path, baseName + ".conf");
            JSONParser jsonParser = new JSONParser();
            LOG.info(String.format("Loading configuration file '%s' ...", configurationFileName));
            Object object = jsonParser.parse(new FileReader(configurationFileName));

            JSONObject jsonObject = (JSONObject) object;
            String pluginPriority = (String) jsonObject.get(DF_PLUGIN_PRIORITY);
            Boolean pluginDropMessage = (Boolean) jsonObject.get(DF_PLUGIN_DROP_MESSAGE);
            Boolean pluginDisabled = (Boolean) jsonObject.get(DF_PLUGIN_DISABLED);
            metrics = (HashMap) jsonObject.get("metrics");
            String fieldName = "cpu.average";
            Set<String> keys = metrics.keySet();
            for(String key : keys)
            {
                Map metric = (Map) metrics.get(key);
                LOG.info(String.format("%s [type %s] [range %s .. %s]", key, metric.get("type").toString(), metric.get("minValue").toString(), metric.get("maxValue").toString()));
            }
            // set configuration
            Map<String, Object> map = new HashMap<>();
            map.put(DF_PLUGIN_PRIORITY, pluginPriority);
            map.put(DF_PLUGIN_DROP_MESSAGE, pluginDropMessage);
            map.put(DF_PLUGIN_DISABLED, pluginDisabled);
            //map.put(DF_PLUGIN_METRICS, metrics);
            
            initialize(new Configuration(map));
        }
        catch(IOException ex)
        {
            LOG.error(String.format("[%d] Initialising plugin FAILED. Filter will be disabled.\r\n%s\r\n", Thread.currentThread().getId(), ex.getMessage()));
            LOG.error("*** " + DF_PLUGIN_NAME + "::dfchBizExecScript() - IOException - Filter will be disabled.");
            ex.printStackTrace();
        }
        catch(Exception ex)
        {
            LOG.error(String.format("[%d] Initialising plugin FAILED. Filter will be disabled.\r\n", Thread.currentThread().getId()));
            LOG.error("*** " + DF_PLUGIN_NAME + "::dfchBizExecScript() - Exception - Filter will be disabled.");
            ex.printStackTrace();
        }
    }

    // we define an 'initialize' method so it is similar to the plugin types with UI configuration options
    private void initialize(final Configuration configuration)
    {
        try
        {
            this.configuration = configuration;
            LOG.trace(String.format("DF_PLUGIN_PRIORITY       : %d\r\n", Integer.parseInt(configuration.getString(DF_PLUGIN_PRIORITY))));
            LOG.trace(String.format("DF_PLUGIN_DROP_MESSAGE   : %b\r\n", configuration.getBoolean(DF_PLUGIN_DROP_MESSAGE)));
            LOG.trace(String.format("DF_PLUGIN_DISABLED       : %b\r\n", configuration.getBoolean(DF_PLUGIN_DISABLED)));

            dropMessage.set(configuration.getBoolean(DF_PLUGIN_DROP_MESSAGE));
            isRunning.set(!configuration.getBoolean(DF_PLUGIN_DISABLED));

            LOG.info(String.format("[%d] Initialising plugin SUCCEEDED. Configuration loaded from '%s'. \r\n", Thread.currentThread().getId(), configurationFileName));
        }
        catch(Exception ex)
        {
            LOG.error("*** " + DF_PLUGIN_NAME + "::initialize() - Exception");
            ex.printStackTrace();
        }
    }
    @Override
    public boolean filter(Message msg)
    {
        if(!isRunning.get())
        {
            return false;       // false ==  process message
        }
        if(dropMessage.get())
        {
            return true;       // true ==  drop message
        }
        try
        {
            LOG.info(String.format("%s: %s", msg.getId(), msg.getMessage()));

            Map<String, Object> fields = msg.getFields();
            for( Map.Entry field : fields.entrySet())
            {
                String fieldName = field.getKey().toString();
                if(!metrics.containsKey(fieldName))
                {
                    continue;
                }
                LOG.trace(String.format("%s: Validating %s ...", msg.getId(), fieldName));
                Map metric = (Map) metrics.get(fieldName);
                String metricType = metric.get("type").toString();
                try
                {
                    switch(metricType)
                    {
                        case "short":
                        case "long":
                        case "int":
                        case "integer":
                        {
                            long fieldValue = (long) field.getValue();
                            long minValue = (long) metric.get("minValue");
                            long maxValue = (long) metric.get("maxValue");
                            if(fieldValue < minValue || fieldValue > maxValue)
                            {
                                LOG.error(String.format("%s %s: Parameter validation FAILED. [ %d < %d || %d > %d ]", msg.getId(), fieldName, fieldValue, minValue, fieldValue, maxValue));
                                return true;
                            };
                            continue;
                        }
                        case "number":
                        case "float":
                        case "double":
                            double fieldValue = (double) field.getValue();
                            double minValue = (double) metric.get("minValue");
                            double maxValue = (double) metric.get("maxValue");
                            if(fieldValue < minValue || fieldValue > maxValue)
                            {
                                LOG.error(String.format("%s %s: Parameter validation FAILED. [ %d < %d || %d > %d ]", msg.getId(), fieldName, fieldValue, minValue, fieldValue, maxValue));
                                return true;
                            };
                            continue;
                        default:
                            continue;
                    }
                }
                catch(Exception ex)
                {
                    LOG.error(String.format("%s Parameter validation FAILED. [ !(%s instanceof %s) ]", msg.getId(), fieldName, metricType));
                    return true;
                }
            }
        }
        catch (Exception ex)
        {
            LOG.error(ex.getMessage());
            ex.printStackTrace();
            return true;    // true == drop message
        }
        return false;       // false ==  process message
    }

    @Override
    public String getName()
    {
        return (new metricsValidationMetadata()).getName();
    }
    @Override
    public int getPriority()
    {
        if(!isRunning.get())
        {
            // if the plugin is disabled set it to the lowest priority
            return 99;
        }
        return Integer.parseInt(configuration.getString(DF_PLUGIN_PRIORITY));
    }


}

/**
 *
 *
 * Copyright 2015 Ronald Rink, d-fens GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

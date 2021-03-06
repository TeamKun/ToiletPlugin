package net.kunmc.lab.toiletplugin.game.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.naming.SizeLimitExceededException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ConfigManager
{
    @Getter
    private final Object config;
    private final Class<?> configClass;
    @Getter
    private final HashMap<String, GeneratedConfig> map;

    public ConfigManager(Object config)
    {
        this.config = config;
        this.configClass = config.getClass();
        this.map = new HashMap<>();
        this.generateMap();
    }

    private void generateMap()
    {
        List<GeneratedConfig> configs = getConfigs();
        configs.forEach(config -> map.put(config.getField().getName(), config));
    }

    public boolean isConfigExist(String name)
    {
        if (map.containsKey(name))
            return true;

        if (name.length() < 4)
            return false;
        String n = "min" + name.substring(0, 1).toUpperCase() + name.substring(1);
        GeneratedConfig config = map.get(n);
        if (config == null)
            return false;

        return config.define.ranged();
    }

    public List<GeneratedConfig> getConfigs()
    {
        List<GeneratedConfig> configs = new ArrayList<>();
        for (Field declaredField : configClass.getDeclaredFields())
        {
            if (!declaredField.isAnnotationPresent(Config.class))
                continue;
            declaredField.setAccessible(true);

            configs.add(new GeneratedConfig(
                    this.config,
                    declaredField,
                    declaredField.getAnnotation(Config.class)
            ));
        }

        return configs;
    }

    public void checkValid(String name, String value)
            throws NoSuchFieldException, SizeLimitExceededException, NegativeArraySizeException,
            IllegalArgumentException, ClassNotFoundException
    {
        if (!map.containsKey(name))
            throw new NoSuchFieldException("No config has found with name: " + name);

        GeneratedConfig config = map.get(name);

        if (config.getDefine().enums().length != 0)
        {
            List<String> enums = new ArrayList<>(Arrays.asList(config.getDefine().enums()));
            if (!enums.contains(value))
                throw new ClassNotFoundException("The value is not in the enum list");
        }

        double min = config.getDefine().min();
        double max = config.getDefine().max();

        if (config.getField().getType() == Long.class || config.getField().getType() == long.class)
        {
            long longValue = Long.parseLong(value);
            if (min != -1 && longValue < min)
                throw new NegativeArraySizeException("The value is less than the min value: " + name);
            if (max != -1 && longValue > max)
                throw new SizeLimitExceededException("The value is greater than the max value: " + name);
        }
        else if (config.getField().getType() == Integer.class || config.getField().getType() == int.class)
        {
            int intValue = Integer.parseInt(value);
            if (min != -1 && intValue < min)
                throw new NegativeArraySizeException("The value is less than the min value: " + name);
            if (max != -1 && intValue > max)
                throw new SizeLimitExceededException("The value is greater than the max value: " + name);
        }
        else if (config.getField().getType() == Double.class || config.getField().getType() == double.class)
        {
            double doubleValue = Double.parseDouble(value);
            if (min != -1 && doubleValue < min)
                throw new NegativeArraySizeException("The value is less than the min value: " + name);
            if (max != -1 && doubleValue > max)
                throw new SizeLimitExceededException("The value is greater than the max value: " + name);
        }
        else if (config.getField().getType() == String.class)
        {
            if (min != -1 && value.length() < min)
                throw new NegativeArraySizeException("The value is less than the min value: " + name);
            if (max != -1 && value.length() > max)
                throw new SizeLimitExceededException("The value is greater than the max value: " + name);
        }
        else if (config.getField().getType() == Boolean.class || config.getField().getType() == boolean.class)
        {
            if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false"))
                throw new IllegalArgumentException("The value is not a boolean: " + name);
        }
        else if (config.getField().getType().isEnum())
        {
            if (Arrays.stream(config.getField().getType().getEnumConstants())
                    .map(o -> (Enum<?>) o)
                    .map(Enum::name)
                    .noneMatch(s -> s.equalsIgnoreCase(value)))
                throw new ClassNotFoundException("The value is not in the enum list");
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public boolean setValue(String name, String value)
            throws NoSuchFieldException, SizeLimitExceededException, NegativeArraySizeException,
            IllegalArgumentException, ClassNotFoundException
    {
        checkValid(name, value);

        GeneratedConfig config = map.get(name);
        if (config.getField().getType() == Long.class || config.getField().getType() == long.class)
            config.setValue(Long.parseLong(value));
        else if (config.getField().getType() == Integer.class || config.getField().getType() == int.class)
            config.setValue(Integer.parseInt(value));
        else if (config.getField().getType() == Double.class || config.getField().getType() == double.class)
            config.setValue(Double.parseDouble(value));
        else if (config.getField().getType() == String.class)
            config.setValue(value);
        else if (config.getField().getType() == Boolean.class || config.getField().getType() == boolean.class)
            config.setValue(Boolean.parseBoolean(value));
        else if (config.getField().getType().isEnum())
            config.setValue(Enum.valueOf((Class<Enum>) config.getField().getType(), value));
        else
            return false;
        return true;
    }

    public GeneratedConfig getConfig(String name)
    {
        if (!map.containsKey(name))
            return null;
        return map.get(name);
    }

    public GeneratedConfig getConfigAllowRanged(String name, boolean isMax)
    {
        if (map.containsKey(name))
            return map.get(name);

        if (name.length() < 1)
            return null;

        String prefix = isMax ? "max": "min";
        name = prefix + name.substring(0, 1).toUpperCase() + name.substring(1);
        if (map.containsKey(name))
            return map.get(name);
        return null;
    }

    public GeneratedConfig getConfigAllowRangedAny(String name)
    {
        GeneratedConfig config = getConfigAllowRanged(name, false);
        if (config != null)
            return config;
        return getConfigAllowRanged(name, true);
    }

    @AllArgsConstructor
    public static class GeneratedConfig
    {
        private final Object clazz;
        @Getter
        private final Field field;

        @Getter
        Config define;

        public Object getValue()
        {
            try
            {
                return field.get(clazz);
            }
            catch (IllegalAccessException e)
            {
                e.printStackTrace();
                return null;
            }
        }

        public boolean setValue(Object value)
        {
            try
            {
                field.set(clazz, value);
                return true;
            }
            catch (IllegalAccessException e)
            {
                e.printStackTrace();
                return false;
            }
        }

        public Integer intValue()
        {
            return (Integer) getValue();
        }

        public Boolean booleanValue()
        {
            return (Boolean) getValue();
        }

        public String stringValue()
        {
            return (String) getValue();
        }
    }
}

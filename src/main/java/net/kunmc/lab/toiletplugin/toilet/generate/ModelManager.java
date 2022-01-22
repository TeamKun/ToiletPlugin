package net.kunmc.lab.toiletplugin.toilet.generate;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public class ModelManager
{
    private final HashMap<String, File> models;

    public ModelManager()
    {
        this.models = new HashMap<>();
    }

    public File fromName(String name)
    {
        return models.get(name);
    }

    public boolean contains(String name)
    {
        return models.containsKey(name);
    }

    public List<String> getNames()
    {
        return models.keySet().stream().sorted().collect(java.util.stream.Collectors.toList());
    }

    public void scan(File folder)
    {
        File[] files = folder.listFiles();
        if (files != null)
        {
            for (File file : files)
            {
                if (file.isDirectory())
                    scan(file);
                else
                {
                    String name = file.getName();
                    if (name.endsWith(".nbt"))
                        models.put(name.substring(0, name.length() - 4), file);
                    if (name.endsWith(".schematic"))
                        models.put(name.substring(0, name.length() - 11), file);
                }
            }
        }
    }
}

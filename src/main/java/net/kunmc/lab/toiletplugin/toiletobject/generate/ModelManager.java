package net.kunmc.lab.toiletplugin.toiletobject.generate;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

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
        return models.keySet().stream().sorted().collect(Collectors.toList());
    }

    public void scan(File folder, String prefix)
    {
        File[] files = folder.listFiles();
        if (files != null)
        {
            for (File file : files)
            {
                if (file.isDirectory())
                {
                    if (prefix == null)
                        scan(file, file.getName());
                    else
                        scan(file, prefix);
                }
                else
                {
                    String name = file.getName();
                    if (name.endsWith(".nbt"))
                        models.put((prefix == null ? "": prefix + ":") + name.substring(0, name.length() - 4), file);
                    if (name.endsWith(".schematic"))
                        models.put((prefix == null ? "": prefix + ":") + name.substring(0, name.length() - 11), file);
                }
            }
        }
    }

    public void scan(File folder)
    {
        scan(folder, null);
    }

    public void clear()
    {
        models.clear();
    }
}

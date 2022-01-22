package net.kunmc.lab.toiletplugin.utils;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class Pair<L, R>
{
    private L left;
    private R right;

    public Pair(L left, R right)
    {
        this.left = left;
        this.right = right;
    }

    public static <L, R> Pair<L, R> of(L left, R right)
    {
        return new Pair<>(left, right);
    }
}

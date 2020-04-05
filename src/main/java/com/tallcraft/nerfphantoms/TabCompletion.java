package com.tallcraft.nerfphantoms;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TabCompletion implements TabCompleter {
    @Override
    public List<String> onTabComplete (CommandSender sender, Command cmd, String label, String[] args){
        if(cmd.getName().equalsIgnoreCase("nerfphantoms") && args.length >= 0){
            if(sender instanceof Player){
                Player player = (Player) sender;

                List<String> list = new ArrayList<>();
                list.add("help");

                if(player.hasPermission("nerfphantoms.disablespawn.self")) {
                    list.add("togglespawn");
                }

                if(player.hasPermission("nerfphantoms.reload")) {
                    list.add("reload");
                }

                if(player.hasPermission("nerfphantoms.kill")) {
                    list.add("kill");
                }

                return list;

            }
        }
        return null;
    }
}
package com.tallcraft.nerfphantoms;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TabCompletion implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("nerfphantoms") && args.length == 1) {
            List<String> list = new ArrayList<String>();
            list.add("help");

            if (sender.hasPermission("nerfphantoms.disablespawn.self")) {
                list.add("togglespawn");
                list.add("disablespawn");
                list.add("enablespawn");
            }

            if (sender.hasPermission("nerfphantoms.reload")) {
                list.add("reload");
            }

            if (sender.hasPermission("nerfphantoms.kill")) {
                list.add("kill");
            }
            List<String> output = new ArrayList<String>();
            for (String command : list) {
                if (command.startsWith(args[0])) {
                    output.add(command);
                }
            }
            return output;
        }
        List<String> list = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            list.add(p.getName());
        }
        return list;
    }
}
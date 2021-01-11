package me.gallent.hsupoints2;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

public class PointTab implements TabCompleter, Listener {
    public PointCounter2 plugin;
    public Player player;
    public List<String> tabEmpty = new ArrayList<String>();


    public PointTab(PointCounter2 plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length==1) {
            List<String> tab1 = new ArrayList<String>();
            tab1.add("Points");
            tab1.add("Character");
            tab1.add("Tutorial");

            if(sender.hasPermission("pointCounter.give")) {
                tab1.add("Give");
            }


            List<String> narrow=new ArrayList<String>();
            for (String a : tab1) {
                if(a.toLowerCase().startsWith(args[0].toLowerCase()))
                    narrow.add(a);
            }

            return narrow;
        }
        if(args[0].equalsIgnoreCase("character")||args[0].equalsIgnoreCase("char")) {
            if(args.length==2) {
                List<String> tab1 = new ArrayList<String>();
                tab1.add("Create");
                tab1.add("Switch");
                tab1.add("Delete");
                tab1.add("Rename");

                List<String> narrow=new ArrayList<String>();
                for (String a : tab1) {
                    if(a.toLowerCase().startsWith(args[1].toLowerCase()))
                        narrow.add(a);
                }

                return narrow;
            }
            if(args[1].equalsIgnoreCase("Switch")||args[1].equalsIgnoreCase("Delete")||args[1].equalsIgnoreCase("Rename")) {
                List<String> tabComp = new ArrayList<String>();
                if(args.length==3) {
                    plugin.pAssign(sender);
                    plugin.getPData().getConfigurationSection("characters").getKeys(false).forEach(key -> {
                        tabComp.add(key);
                    });

                    List<String> narrow=new ArrayList<String>();
                    for (String a : tabComp) {
                        if(a.toLowerCase().startsWith(args[2].toLowerCase()))
                            narrow.add(a);
                    }

                    return narrow;
                }
                return tabEmpty;
            }
        }

        if (args[0].equalsIgnoreCase("points")||args[0].equalsIgnoreCase("p")){
            if(args.length==2) {
                List<String> tab1 = new ArrayList<String>();
                tab1.add("Bank");
                tab1.add("Spend");
                tab1.add("View");
                tab1.add("Unlock");
                tab1.add("Withdraw");

                List<String> narrow=new ArrayList<String>();
                for (String a : tab1) {
                    if(a.toLowerCase().startsWith(args[1].toLowerCase()))
                        narrow.add(a);
                }

                return narrow;
            }
            if(args[1].equalsIgnoreCase("View")) {
                if(args.length==3) {
                    List<String> pointTypes = new ArrayList<String>();
                    pointTypes.add("Elemental");
                    pointTypes.add("Cosmos");
                    pointTypes.add("Nature");
                    pointTypes.add("Pyromancy");
                    pointTypes.add("Geomancy");
                    pointTypes.add("Hydromancy");
                    pointTypes.add("Aeromancy");
                    pointTypes.add("Lunimancy");
                    pointTypes.add("Umbramancy");
                    pointTypes.add("Vivimancy");
                    pointTypes.add("Ecomancy");
                    pointTypes.add("Necromancy");
                    pointTypes.add("Health");
                    pointTypes.add("Strength");
                    pointTypes.add("Agility");
                    pointTypes.add("Craftsmanship");
                    pointTypes.add("Magic");
                    pointTypes.add("Physical");
                    pointTypes.add("General");
                    pointTypes.add("All");

                    List<String> narrow=new ArrayList<String>();
                    for (String a : pointTypes) {
                        if(a.toLowerCase().startsWith(args[2].toLowerCase()))
                            narrow.add(a);
                    }

                    return narrow;
                }
                if(args.length==4&&sender.hasPermission("pointCounter.viewOther")) {
                    List<String> playersOn=new ArrayList<String>();
                    for (Player playerOn : Bukkit.getOnlinePlayers()) {
                        playersOn.add(playerOn.getName());
                    }
                    List<String> narrow=new ArrayList<String>();
                    for (String a : playersOn) {
                        if(a.toLowerCase().startsWith(args[3].toLowerCase()))
                            narrow.add(a);
                    }

                    return narrow;

                }

            }
            if(args[1].equalsIgnoreCase("Spend")) {
                if(args.length==4) {
                    List<String> pointTypes = new ArrayList<String>();
                    pointTypes.add("Elemental");
                    pointTypes.add("Cosmos");
                    pointTypes.add("Nature");
                    pointTypes.add("Pyromancy");
                    pointTypes.add("Geomancy");
                    pointTypes.add("Hydromancy");
                    pointTypes.add("Aeromancy");
                    pointTypes.add("Lunimancy");
                    pointTypes.add("Umbramancy");
                    pointTypes.add("Vivimancy");
                    pointTypes.add("Ecomancy");
                    pointTypes.add("Necromancy");
                    pointTypes.add("Health");
                    pointTypes.add("Strength");
                    pointTypes.add("Agility");
                    pointTypes.add("Craftsmanship");

                    List<String> narrow=new ArrayList<String>();
                    for (String a : pointTypes) {
                        if(a.toLowerCase().startsWith(args[3].toLowerCase()))
                            narrow.add(a);
                    }

                    return narrow;
                }
                if(args.length==3) {
                    List<String> value = new ArrayList<String>();
                    value.add("1");
                    List<String> narrow=new ArrayList<String>();
                    for (String a : value) {
                        if(a.toLowerCase().startsWith(args[2].toLowerCase()))
                            narrow.add(a);
                    }

                    return narrow;
                }
            }
            if(args[1].equalsIgnoreCase("Bank")&&args.length==3) {
                List<String> value = new ArrayList<String>();
                value.add("1");
                List<String> narrow=new ArrayList<String>();
                for (String a : value) {
                    if(a.toLowerCase().startsWith(args[2].toLowerCase()))
                        narrow.add(a);
                }

                return narrow;
            }
            if(args[1].equalsIgnoreCase("Unlock")&&args.length==3) {
                List<String> pointTypes = new ArrayList<String>();
                pointTypes.add("Pyromancy");
                pointTypes.add("Geomancy");
                pointTypes.add("Hydromancy");
                pointTypes.add("Aeromancy");
                pointTypes.add("Lunimancy");
                pointTypes.add("Umbramancy");
                pointTypes.add("Vivimancy");
                pointTypes.add("Ecomancy");
                pointTypes.add("Necromancy");

                List<String> narrow=new ArrayList<String>();
                for (String a : pointTypes) {
                    if(a.toLowerCase().startsWith(args[2].toLowerCase()))
                        narrow.add(a);
                }

                return narrow;
            }
            if(args[1].equalsIgnoreCase("Withdraw")) {
                if(args.length==3) {
                    List<String> value = new ArrayList<String>();
                    value.add("1");
                    List<String> narrow=new ArrayList<String>();
                    for (String a : value) {
                        if(a.toLowerCase().startsWith(args[2].toLowerCase()))
                            narrow.add(a);
                    }

                    return narrow;
                }
                if(args.length==4) {
                    List<String> pointTypes = new ArrayList<String>();
                    pointTypes.add("Magic");
                    pointTypes.add("General");
                    pointTypes.add("Physical");

                    List<String> narrow=new ArrayList<String>();
                    for (String a : pointTypes) {
                        if(a.toLowerCase().startsWith(args[3].toLowerCase()))
                            narrow.add(a);
                    }

                    return narrow;
                }
            }
        }

        if(args[0].equalsIgnoreCase("give")&&sender.hasPermission("pointCounter.give")) {
            if(args.length==2) {
                List<String> pointTypes = new ArrayList<String>();
                pointTypes.add("Magic");
                pointTypes.add("Physical");
                pointTypes.add("General");

                List<String> narrow=new ArrayList<String>();
                for (String a : pointTypes) {
                    if(a.toLowerCase().startsWith(args[1].toLowerCase()))
                        narrow.add(a);
                }

                return narrow;

            }
            if(args.length==3) {
                List<String> value = new ArrayList<String>();
                value.add("1");
                List<String> narrow=new ArrayList<String>();
                for (String a : value) {
                    if(a.toLowerCase().startsWith(args[2].toLowerCase()))
                        narrow.add(a);
                }

                return narrow;

            }

            if(args.length==4) {
                List<String> playersOn=new ArrayList<String>();
                for (Player playerOn : Bukkit.getOnlinePlayers()) {
                    playersOn.add(playerOn.getName());
                }
                List<String> narrow=new ArrayList<String>();
                for (String a : playersOn) {
                    if(a.toLowerCase().startsWith(args[3].toLowerCase()))
                        narrow.add(a);
                }

                return narrow;

            }


        }

        return tabEmpty;
    }
}

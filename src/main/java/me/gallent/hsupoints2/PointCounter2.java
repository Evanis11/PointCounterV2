package me.gallent.hsupoints2;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class PointCounter2 extends JavaPlugin implements Listener {
    public PointSystem pointSys = new PointSystem(this);
    public PointTab pointTab=new PointTab(this);
    public File playerDataFolder = null;
    public Player player;
    public static PointCounter2 instance;
    public String charName;
    public FileConfiguration pdataConfig = null;
    public FileConfiguration tardataConfig = null;
    public File playerFile = null;
    public File targetFile = null;
    public Player target;
    public ArrayList<String> characters = new ArrayList<String>();
    public CommandSender sendB;
    private String stringChar;
    private Boolean success;
    private String charSel;
    private String newChar;
    public String varA = null;
    public String varB = null;

    public Inventory delConfirm;
    public Inventory aversions;


    public static PointCounter2 getInstance() {
        return instance;
    }

    //construction
    @Override
    public void onEnable() {
        this.getCommand("pointcounter").setTabCompleter(pointTab);
        saveDefaultConfig();
        playerDataFolder = new File(getDataFolder() + File.separator + "playerdata");
        //if the player data folder doesn't exist, make it
        if (!playerDataFolder.exists()) {
            playerDataFolder.mkdirs();
        }
        getServer().getPluginManager().registerEvents(this, this);
        instance = this;
    }

    @Override
    public void onDisable() {

    }

    //command stuff

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("PointCounter") || label.equalsIgnoreCase("PC")) {
            if (args.length == 0) {
                //Basic plugin info
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lHSU Point Counter"));
                sender.sendMessage(ChatColor.GREEN + "Version 2.0");
                sender.sendMessage(ChatColor.GREEN + "By Gallent");
                sender.sendMessage("");
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a&oCommands:"));
                sender.sendMessage(ChatColor.GOLD + "PointCounter Character (char)");
                sender.sendMessage(ChatColor.GOLD + "PointCounter Points (p)");
                sender.sendMessage(ChatColor.GOLD + "PointCounter Tutorial (tut)");
                return true;
            }
            switch (args[0].toLowerCase()) {
                //switch to indicate specific command

                //character commands
                case "character":
                case "char":
                case "c":
                    if (!(sender.hasPermission("pointCounter.basic"))) {
                        sender.sendMessage("You cannot use this");
                        return true;
                    }
                    switch (args[1].toLowerCase()) {
                        case "create":
                            //player check
                            if (!(sender instanceof Player)) {
                                sender.sendMessage(ChatColor.RED + "Only players can create characters");
                                return true;
                            }

                            //syntax check
                            if (args.length == 2) {
                                sender.sendMessage(ChatColor.RED + "Use: /PointCounter Character Create <name>");
                                return true;
                            }


                            //Set charName to nothing, add strings to charName based on length of args
                            this.charName = "";
                            for (int i = 2; i < args.length; i++) {
                                if (!charName.equalsIgnoreCase("")) {
                                    charName = charName + " ";
                                }
                                String namePiece = args[i];
                                this.charName = charName + namePiece;
                            }

                            pAssign(sender);

                            if (!charCheck(charName)) {
                                getPData().set("characters." + charName, " ");
                                getPData().set("ActiveCharacter", charName);

                                pointSys.createPoints();

                                savePData();

                                player.sendMessage(ChatColor.GREEN + charName + " created successfully.");
                                return true;
                            }
                            player.sendMessage(ChatColor.RED + "This character already exists.");
                            return true;

                        case "switch":
                            if (!(sender instanceof Player)) {
                                sender.sendMessage(ChatColor.RED + "The Console has no characters.");
                                return true;
                            }
                            if (args.length == 2) {
                                sender.sendMessage(ChatColor.RED + "Use: /PointCounter Character Switch <Name>");
                                return true;
                            }
                            pAssign(sender);
                            this.charSel = "";
                            for (int i = 2; i < args.length; i++) {
                                if (!charSel.equalsIgnoreCase("")) {
                                    charSel = charSel + " ";
                                }
                                String namePiece = args[i];
                                this.charSel = charSel + namePiece;
                            }
                            if (charCheck(charSel)) {
                                if (getPData().getString("ActiveCharacter").equalsIgnoreCase(stringChar)) {
                                    player.sendMessage(ChatColor.RED + "You are already this character.");
                                    return true;
                                }
                                String oldChar = getPData().getString("ActiveCharacter");
                                getPData().set("ActiveCharacter", stringChar);
                                player.sendMessage(ChatColor.GREEN + "You switched from " + oldChar + " to " + stringChar);
                                savePData();
                                return true;
                            }
                            player.sendMessage(ChatColor.RED + "You do not have a character named " + charSel);
                            return true;

                        case "delete":
                        case "del":
                            if (!(sender instanceof Player)) {
                                sender.sendMessage(ChatColor.RED + "The Console has no characters to delete.");
                                sender.sendMessage(ChatColor.RED + "To delete someone else's characters, edit their config file for now.");
                                return true;
                            }
                            pAssign(sender);
                            if (args.length == 2) {
                                sender.sendMessage(ChatColor.RED + "Use: /PointCounter Character Delete <Name>");
                                return true;
                            }
                            this.charSel = "";
                            for (int i = 2; i < args.length; i++) {
                                if (!charSel.equalsIgnoreCase("")) {
                                    charSel = charSel + " ";
                                }
                                String namePiece = args[i];
                                this.charSel = charSel + namePiece;
                            }
                            if (charCheck(charSel)) {
                                if (getPData().getString("ActiveCharacter").equalsIgnoreCase(this.stringChar)) {
                                    player.sendMessage(ChatColor.RED + "You cannot delete your current character");
                                    return true;
                                }
                                createDelConfirm();
                                getPData().set("DeleteCharacter", stringChar);
                                savePData();
                                player.openInventory(delConfirm);
                                return true;
                            }
                            player.sendMessage(ChatColor.RED + "You do not have a character named " + charSel);
                            return true;

                        case "rename":
                            if (!(sender instanceof Player)) {
                                sender.sendMessage(ChatColor.RED + "The Console has no characters to rename.");
                                sender.sendMessage(ChatColor.RED + "To rename someone else's characters, edit their config file for now.");
                                return true;
                            }
                            pAssign(sender);
                            if (args.length == 2 || args.length == 3) {
                                sender.sendMessage(ChatColor.RED + "Use: /PointCounter Character rename <Old Name> <New Name>");
                                return true;
                            }
                            int i = 0;
                            Boolean charOne = false;
                            this.charSel = "";
                            for (i = 2; i < args.length; i++) {
                                if (!charSel.equalsIgnoreCase("")) {
                                    charSel = charSel + " ";
                                }
                                String namePiece = args[i];
                                this.charSel = charSel + namePiece;
                                if (charCheck(charSel)) {
                                    charOne = true;
                                    break;
                                }
                            }
                            this.newChar = "";
                            if (!charOne == true) {
                                player.sendMessage(ChatColor.RED + "You do not have a character of that name");
                            }
                            for (i++; i < args.length; i++) {
                                if (!newChar.equalsIgnoreCase("")) {
                                    newChar = newChar + " ";
                                }
                                String namePiece = args[i];
                                newChar = newChar + namePiece;
                            }


                            if (charCheck(newChar)) {
                                player.sendMessage(ChatColor.RED + "You already have a character named " + newChar);
                                return true;
                            }

                            if (charCheck(this.charSel)) {
                                if (getPData().getString("ActiveCharacter").equalsIgnoreCase(this.stringChar)) {
                                    ConfigurationSection section = getPData().getConfigurationSection("characters." + stringChar);
                                    section.getKeys(true).forEach(key -> {
                                        if (PCFunctions.isInt(section.getString(key))) {
                                            int intValue = section.getInt(key);
                                            getPData().set("characters." + newChar + "." + key, intValue);
                                            return;
                                        }
                                        if (section.isBoolean(key)) {
                                            getPData().set("characters." + newChar + "." + key, section.getBoolean(key));
                                            return;
                                        }

                                        String stringValue = section.getString(key);
                                        getPData().set("characters." + newChar + "." + key, stringValue);
                                        Bukkit.getConsoleSender().sendMessage("PingTest");
                                    });
                                    getPData().set("characters." + stringChar, null);
                                    getPData().set("ActiveCharacter", newChar);
                                    savePData();
                                    player.sendMessage(ChatColor.GREEN + "Renamed " + stringChar + " To " + newChar);
                                    return true;
                                }
                                ConfigurationSection section = getPData().getConfigurationSection("characters." + stringChar);
                                section.getKeys(true).forEach(key -> {
                                    int test = section.getInt(key);
                                    getPData().set("characters." + newChar + "." + key, test);
                                });
                                getPData().set("characters." + stringChar, null);
                                savePData();
                                return true;
                            }
                            return true;

                        default:
                            sender.sendMessage(ChatColor.RED + "Use: /PointCounter Character <Create|Switch|Delete|Rename>");
                            return true;


                    }

                    //points
                case "points":
                case "p":
                    //permissions check
                    if (!(sender.hasPermission("pointCounter.basic"))) {
                        sender.sendMessage("You cannot use this");
                        return true;
                    }

                    if (args.length == 1) {
                        sender.sendMessage(ChatColor.RED + "Use: /PointCounter give <type> <amount> <player (optional)>");
                        return true;
                    }
                    //banking
                    switch (args[1].toLowerCase()) {
                        case "bank":
                            if (!(sender instanceof Player)) {
                                sender.sendMessage(ChatColor.RED + "The console does not have any points to bank.");
                                return true;
                            }
                            if (args.length == 2) {
                                sender.sendMessage(ChatColor.RED + "Use: /Pointcounter Points Bank <Amount>");
                                return true;
                            }
                            this.varA = args[2].toString();
                            pAssign(sender);
                            if (PCFunctions.isInt(varA) && Integer.valueOf(varA) <= 0) {
                                player.sendMessage(ChatColor.RED + "Must be greater than zero");
                                return true;
                            }
                            if (!(getPData().contains("ActiveCharacter"))) {
                                player.sendMessage(ChatColor.RED + "You need a character before you can use this.");
                                return true;
                            }
                            pointSys.bank();
                            return true;

                        //spending
                        case "spend":
                            if (!(sender instanceof Player)) {
                                sender.sendMessage("The console does not have points to spend or stats to spend on.");
                                return true;
                            }
                            if (args.length == 2 || args.length == 3) {
                                sender.sendMessage(ChatColor.RED + "Use: /Pointcounter Points spend <Amount> <Category>");
                                return true;
                            }
                            this.varA = args[2].toString();
                            this.varB = args[3].toString();
                            pAssign(sender);
                            if (PCFunctions.isInt(varA) && Integer.valueOf(varA) <= 0) {
                                player.sendMessage(ChatColor.RED + "Must be greater than zero");
                                return true;
                            }
                            if (!(getPData().contains("ActiveCharacter"))) {
                                player.sendMessage(ChatColor.RED + "You need a character before you can use this.");
                                return true;
                            }
                            pointSys.spend();
                            return true;
                        //viewing
                        case "view":
                            //view other
                            if (sender.hasPermission("pointCounter.viewOther") && args.length == 4) {
                                this.varB = args[2].toString();
                                this.target = Bukkit.getPlayer(args[3]);
                                this.player = this.target;
                                this.sendB = sender;
                                reloadTarData();
                                reloadPData();
                                this.playerFile = new File(playerDataFolder, target.getUniqueId().toString() + ".yml");
                                Bukkit.getConsoleSender().sendMessage(this.playerFile.toString());
                                if (this.target == null) {
                                    sender.sendMessage(ChatColor.RED + "Can't find player by the name of " + args[3]);
                                }
                                Bukkit.getConsoleSender().sendMessage(getPData().getString("ActiveCharacter"));
                                if (!(getTarData().contains("ActiveCharacter"))) {
                                    sender.sendMessage(ChatColor.RED + "Target has no active character.");
                                    return true;
                                }

                                pointSys.view2();
                                return true;

                            }


                            if (!(sender instanceof Player)) {
                                sender.sendMessage(ChatColor.RED + "The console does not have any points to view.");
                                return true;
                            }
                            if (args.length == 2) {
                                sender.sendMessage(ChatColor.RED + "Use: /Pointcounter Points View <Category>");
                                return true;
                            }
                            this.varB = args[2].toString();
                            pAssign(sender);
                            if (!(getPData().contains("ActiveCharacter"))) {
                                player.sendMessage(ChatColor.RED + "You need a character before you can use this.");
                                return true;
                            }
                            pointSys.view();
                            return true;

                        //unlocking
                        case "unlock":
                            if (!(sender instanceof Player)) {
                                sender.sendMessage(ChatColor.RED + "The console does not have any schools to unlock.");
                                return true;
                            }
                            if (args.length == 2) {
                                sender.sendMessage(ChatColor.RED + "Use: /Pointcounter Points Unlock <Category>");
                                return true;
                            }
                            this.varB = args[2].toString();
                            pAssign(sender);
                            if (!(getPData().contains("ActiveCharacter"))) {
                                player.sendMessage(ChatColor.RED + "You need a character before you can use this.");
                                return true;
                            }
                            pointSys.unlock();
                            return true;

                        //withdrawing
                        case "withdraw":
                            if (!(sender instanceof Player)) {
                                sender.sendMessage(ChatColor.RED + "You cannot withdraw points from the console.");
                                return true;
                            }
                            pAssign(sender);
                            if (!(getPData().contains("ActiveCharacter"))) {
                                player.sendMessage(ChatColor.RED + "You need a character before you can use this.");
                                return true;
                            }
                            String activeChar = getPData().getString("ActiveCharacter");
                            ConfigurationSection unspent = getPData().getConfigurationSection("characters." + activeChar + ".points.unspent");
                            if (player.getInventory().firstEmpty() == -1) {
                                player.sendMessage("Inventory full");
                                return true;
                            }
                            pointSys.pointItems();
                            if (args.length == 4) {
                                if (!PCFunctions.isInt(args[2])) {
                                    player.sendMessage(ChatColor.RED + args[2] + " is not a number.");
                                    return true;
                                }
                                Integer withdrawAmt = Integer.valueOf(args[2]);
                                if (args[3].equalsIgnoreCase("magic")) {

                                    if (unspent.getInt("magic") - withdrawAmt >= 0) {
                                        unspent.set("magic", unspent.getInt("magic") - withdrawAmt);
                                        pointSys.mPoint.setAmount(withdrawAmt);
                                        player.getInventory().addItem(pointSys.mPoint);
                                        savePData();
                                        player.sendMessage((withdrawAmt == 1) ? ChatColor.GREEN + "You have withdrawn one magic point" : ChatColor.GREEN + "You have withdrawn " + withdrawAmt + " magic points");
                                        return true;
                                    }
                                    player.sendMessage((unspent.getInt("magic") > 0) ? ChatColor.RED + "You do not have " + withdrawAmt + " unspent magic points." : ChatColor.RED + "You have no unspent magic points.");
                                    return true;
                                }
                                if (args[3].equalsIgnoreCase("physical") || args[3].equalsIgnoreCase("Stat")) {
                                    if (unspent.getInt("physical") - withdrawAmt >= 0) {
                                        unspent.set("physical", unspent.getInt("physical") - withdrawAmt);
                                        pointSys.sPoint.setAmount(withdrawAmt);
                                        player.getInventory().addItem(pointSys.sPoint);
                                        savePData();
                                        player.sendMessage((withdrawAmt == 1) ? ChatColor.GREEN + "You have withdrawn one stat point" : ChatColor.GREEN + "You have withdrawn " + withdrawAmt + " stat points");
                                        return true;
                                    }
                                    player.sendMessage((unspent.getInt("physical") > 0) ? ChatColor.RED + "You do not have " + withdrawAmt + " unspent stat points." : ChatColor.RED + "You have no unspent stat points.");
                                    return true;
                                }
                                if (args[3].equalsIgnoreCase("general") || args[3].equalsIgnoreCase("universal")) {
                                    if (unspent.getInt("general") - withdrawAmt >= 0) {
                                        unspent.set("general", unspent.getInt("general") - withdrawAmt);
                                        pointSys.sPoint.setAmount(withdrawAmt);
                                        player.getInventory().addItem(pointSys.gPoint);
                                        savePData();
                                        player.sendMessage((withdrawAmt == 1) ? ChatColor.GREEN + "You have withdrawn one general point" : ChatColor.GREEN + "You have withdrawn " + withdrawAmt + " general points");
                                        return true;
                                    }
                                    player.sendMessage((unspent.getInt("general") > 0) ? ChatColor.RED + "You do not have " + withdrawAmt + " unspent general points." : ChatColor.RED + "You have no unspent general points.");
                                    return true;
                                }
                                player.sendMessage(ChatColor.RED + args[3].toString() + " is not a recognized point type.");
                                return true;
                            }
                            player.sendMessage(ChatColor.RED + "Use: /Pointcounter Points Withdraw <Amount> <Type>");
                            return true;

                        default:
                            sender.sendMessage(ChatColor.RED + "Use: /Pointcounter Points <Bank|Spend|View|Unlock>");
                            return true;
                    }



                    //Give
                case "give":
                    if (!(sender.hasPermission("pointCounter.give"))) {
                        sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                    }
                    if (args.length == 3 && PCFunctions.isInt(args[2])) {

                        if (!(sender instanceof Player)) {
                            sender.sendMessage("you cannot give the console points.");
                            return true;
                        }
                        pAssign(sender);

                        if (player.getInventory().firstEmpty() == -1) {
                            player.sendMessage("Inventory full");
                            return true;
                        }

                        pointSys.pointItems();

                        if (!(PCFunctions.isInt(args[2]))) {
                            sender.sendMessage(ChatColor.RED + "That is not an number value.");
                            return true;

                        }

                        Integer giveAmt = Integer.valueOf(args[2]);
                        switch (args[1].toLowerCase()) {
                            case "magic":
                                pointSys.mPoint.setAmount(giveAmt);
                                player.getInventory().addItem(pointSys.mPoint);
                                if (giveAmt == 1) {
                                    sender.sendMessage(ChatColor.GOLD + "Giving one magic point.");
                                    return true;
                                }
                                player.sendMessage(ChatColor.GOLD + "Giving " + giveAmt + " magic points.");
                                return true;

                            case "physical":
                            case "stat":
                                pointSys.sPoint.setAmount(giveAmt);
                                player.getInventory().addItem(pointSys.sPoint);
                                if (giveAmt == 1) {
                                    sender.sendMessage(ChatColor.GOLD + "Giving one physical point.");
                                    return true;
                                }
                                player.sendMessage(ChatColor.GOLD + "Giving " + giveAmt + " physical points.");
                                return true;

                            case "general":
                                pointSys.gPoint.setAmount(giveAmt);
                                player.getInventory().addItem(pointSys.gPoint);
                                if (giveAmt == 1) {
                                    sender.sendMessage(ChatColor.GOLD + "Giving one general point.");
                                    return true;
                                }
                                player.sendMessage(ChatColor.GOLD + "Giving " + giveAmt + " general points.");
                                return true;

                            default:
                                player.sendMessage(ChatColor.RED + "No points specified.");
                                return true;
                        }
                    }
                    if (args.length == 4 && PCFunctions.isInt(args[2])) {
                        Integer giveAmt = Integer.valueOf(args[2]);
                        Player target = Bukkit.getPlayer(args[3]);
                        if (target == null) {
                            sender.sendMessage(ChatColor.RED + "No player selected");
                            return true;
                        }

                        if (target.getInventory().firstEmpty() == -1) {
                            sender.sendMessage("Inventory full");
                            return true;
                        }
                        switch (args[1].toLowerCase()) {
                            case "magic":
                                pointSys.mPoint.setAmount(giveAmt);
                                target.getInventory().addItem(pointSys.mPoint);
                                if (giveAmt == 1) {
                                    sender.sendMessage(ChatColor.GOLD + "Giving one magic point to " + target.getName());
                                    return true;
                                }
                                sender.sendMessage(ChatColor.GOLD + "Giving " + giveAmt + " magic points to " + target.getName());
                                return true;
                            case "physical":
                            case "stat":
                                pointSys.sPoint.setAmount(giveAmt);
                                target.getInventory().addItem(pointSys.sPoint);
                                if (giveAmt == 1) {
                                    sender.sendMessage(ChatColor.GOLD + "Giving one physical point to " + target.getName());
                                    return true;
                                }
                                sender.sendMessage(ChatColor.GOLD + "Giving " + giveAmt + " physical points to " + target.getName());
                                return true;

                            case "general":
                                pointSys.gPoint.setAmount(giveAmt);
                                target.getInventory().addItem(pointSys.gPoint);
                                if (giveAmt == 1) {
                                    sender.sendMessage(ChatColor.GOLD + "Giving one general point to " + target.getName());
                                    return true;
                                }
                                sender.sendMessage(ChatColor.GOLD + "Giving " + giveAmt + " general points to " + target.getName());
                                return true;

                            default:
                                sender.sendMessage(ChatColor.RED + "No points specified.");
                                return true;
                        }
                    }
                    sender.sendMessage(ChatColor.RED + "Use: /PointCounter give <type> <amount> <player (optional)>");
                    return true;

                    //reload player files
                case "reload":
                    if(sender instanceof Player) {
                        this.player=(Player) sender;
                        reloadPData();
                    }
                    reloadConfig();
                    sender.sendMessage(ChatColor.GREEN+"PointCounter configs reloaded.");
                    return true;

                    //tutorial
                case "tutorial":
                case "tut":
                    if(args.length==2&&args[1].equalsIgnoreCase("2")) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&nPoint Counter Tutorial"));
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Page 2: Character commands"));
                        sender.sendMessage("");
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aTo switch between multiple characters, use \"/Pointcounter Character Switch\" followed by the name of the character you would like to switch to."));
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aTo rename a character, use \"/Pointcounter Character Rename <Old Character Name> <New Character Name>\""));
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aTo delete a character, use \"/Pointcounter Character Delete <Character Name>\". You cannot delete your active character."));
                        sender.sendMessage("");
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Page 2 of 4."));
                        return true;
                    }
                    if(args.length==2&&args[1].equalsIgnoreCase("3")) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&nPoint Counter Tutorial"));
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Page 3: Points"));
                        sender.sendMessage("");
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aBy voting or participating in events, you can earn point items."));
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aThese can be banked to a specific character by holding them in your hand and typing \"/Pointcounter Points Bank <amount>\""));
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aThe point types are as follows:"));
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&dMagic Points &aare used to increase your magic schools. You must advance a tier 1 school (Elemental, Cosmic, Nature) to 50 before you can specialize."));
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bStat Points &aare used to increase your physical stats such as Strength, Agility, Health, and Craftsmanship. These have no prerequisits."));
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6General Points &aare able to be used as either of the above points."));
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aTo use the points, use \"/PointCounter points spend <amount> <category>\"."));
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aTier II magic has to be unlocked first. Once a tier 1 school is at 50, you can type \"/PointCounter Points Unlock <Magic Type>\". You can only unlock 4 magics."));
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aTo see how many points you have in a category, use \"/PointCounter Points View <Category>\"."));
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aAny unspent points can be turned back into items with \"/PointCounter Points Withdraw <Amount> <Magic|Physical|General>\"."));
                        sender.sendMessage("");
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Page 3 of 4."));
                        return true;
                    }
                    if(args.length==2&&args[1].equalsIgnoreCase("4")) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&nPoint Counter Tutorial"));
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Page 4: Tips"));
                        sender.sendMessage("");
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a1. If your character was used on TDC or has an unusual amount of points, an Admin can edit your file to change the values."));
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a2. Many of the commands can be shortened. For example, /PointCounter can be shortened to /pc, /PointCounter Character can be shortened to /pc char, and /PointCounter Points can be shortened to /pc p."));
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a3. Spending points and deleting characters are permanent."));
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a4. Just as on the character application, you start with 40 physical points and 20 magic points already banked. Please allocate them the same as you have on your sheet."));
                        sender.sendMessage("");
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Page 4 of 4."));
                        return true;
                    }
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l&nPoint Counter Tutorial"));
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Page 1: Character creation"));
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Page 1: Please read all pages before starting"));
                    sender.sendMessage("");
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aTo use the plugin, you must first have a character."));
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aStart by using \"/PointCounter Character Create\" followed by the name of your character."));
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aWhen prompted, click the item that matches your race (or racial aversion for custom races.)"));
                    sender.sendMessage("");
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Page 1 of 4"));
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"Type /PointCounter Tutorial <page number> to continue"));
                    return true;



                default:
                    sender.sendMessage(ChatColor.RED + "Unknown PointCounter Command.");
                    return false;
            }
        }
        return false;
    }

    //config information
    public void reloadPData() {
        if (this.playerFile == null) {
            try {
                this.playerFile = new File(playerDataFolder, this.player.getUniqueId().toString() + ".yml");
            } catch (NullPointerException npe) {
                Bukkit.getConsoleSender().sendMessage("Reloaded pdata without player");
            }
        }
        this.pdataConfig = YamlConfiguration.loadConfiguration(this.playerFile);
        InputStream defaultStream = this.getResource("playerdata." + playerFile);
        if (defaultStream != null) {
            YamlConfiguration testConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            this.pdataConfig.setDefaults(testConfig);
        }
    }

    public FileConfiguration getPData() {
        if (this.pdataConfig == null)
            reloadPData();
        return this.pdataConfig;
    }

    public void savePData() {
        if (this.pdataConfig == null || this.playerFile == null) {
            return;
        }
        try {
            this.getPData().save(this.playerFile);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Saving Failed");
        }
    }

    public void reloadTarData() {
        if (this.playerFile == null) {
            try {
                this.playerFile = new File(playerDataFolder, this.target.getUniqueId().toString() + ".yml");
            } catch (NullPointerException npe) {
                Bukkit.getConsoleSender().sendMessage("Reloaded pdata without target");
            }
        }
        this.tardataConfig = YamlConfiguration.loadConfiguration(this.playerFile);
        InputStream defaultStream = getResource("playerdata." + playerFile);
        if (defaultStream != null) {
            YamlConfiguration testConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            this.tardataConfig.setDefaults(testConfig);
        }
    }

    public FileConfiguration getTarData() {
        if (this.tardataConfig == null)
            reloadTarData();
        ;
        return this.tardataConfig;
    }

    public void saveTarData() {
        if (this.tardataConfig == null || this.playerFile == null) {
            return;
        }
        try {
            this.getTarData().save(this.playerFile);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Saving Failed");
        }
    }

    //player check
    public void pAssign(CommandSender p1) {
        player = (Player) p1;
        this.playerFile = new File(playerDataFolder, player.getUniqueId().toString() + ".yml");
        reloadPData();
        return;
    }

    //check if character exists
    public boolean charCheck(String checkName) {
        this.stringChar = null;
        this.characters.clear();
        this.success = false;

        //if the player data file does not contain any characters, return false
        if (!(getPData().contains("characters"))) {
            return false;
        }
        //check all keys in characters
        getPData().getConfigurationSection("characters").getKeys(false).forEach(key -> {
            //if the key is equal to checkName, set stringChar to that character and set success to true.
            if (key.equalsIgnoreCase(checkName)) {
                Bukkit.getConsoleSender().sendMessage("ping");
                this.stringChar = key;
                this.success = true;
            }
        });

        return success;
    }

    //Delete Confirmation
    public void createDelConfirm() {
        delConfirm = Bukkit.createInventory(null, 9, ChatColor.BOLD + "" + ChatColor.GOLD + "Are you sure?");
        ItemStack choiceItem = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta meta = choiceItem.getItemMeta();

        //Confirm
        meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Confirm Delete");
        List<String> delLore = new ArrayList<String>();
        delLore.add(ChatColor.GRAY + "You cannot undo this.");
        meta.setLore(delLore);
        choiceItem.setItemMeta(meta);
        delConfirm.setItem(0, choiceItem);

        //Exit
        choiceItem.setType(Material.EMERALD_BLOCK);
        meta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "EXIT");
        delLore.clear();
        delLore.add(ChatColor.GRAY + "Return to safety.");
        meta.setLore(delLore);
        choiceItem.setItemMeta(meta);
        delConfirm.setItem(8, choiceItem);
    }

    //create aversions
    public void createAversions() {
        aversions = Bukkit.createInventory(null, 9, ChatColor.BOLD + "" + ChatColor.DARK_GREEN + "Choose your Race/Aversion.");
        ItemStack choiceItem = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta meta = choiceItem.getItemMeta();

        //Human
        choiceItem.setType(Material.PLAYER_HEAD);
        meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Human");
        List<String> raceLore = new ArrayList<String>();
        raceLore.add(ChatColor.GRAY + "Aversion: Vivimancy");
        meta.setLore(raceLore);
        choiceItem.setItemMeta(meta);
        aversions.setItem(0, choiceItem);

        //Kami
        choiceItem.setType(Material.BOW);
        meta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Kami");
        raceLore.clear();
        raceLore.add(ChatColor.GRAY + "Aversion: Pyromancy");
        meta.setLore(raceLore);
        choiceItem.setItemMeta(meta);
        aversions.setItem(1, choiceItem);

        choiceItem.setType(Material.PRISMARINE_SHARD);
        meta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Merfolk");
        raceLore.clear();
        raceLore.add(ChatColor.GRAY + "Aversion: Aeromancy");
        meta.setLore(raceLore);
        choiceItem.setItemMeta(meta);
        aversions.setItem(2, choiceItem);

        choiceItem.setType(Material.FIRE_CHARGE);
        meta.setDisplayName(ChatColor.DARK_RED + "" + ChatColor.BOLD + "Demon");
        raceLore.clear();
        raceLore.add(ChatColor.GRAY + "Aversion: Ecomancy");
        meta.setLore(raceLore);
        choiceItem.setItemMeta(meta);
        aversions.setItem(3, choiceItem);

        choiceItem.setType(Material.LAPIS_LAZULI);
        meta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Moon Elf");
        raceLore.clear();
        raceLore.add(ChatColor.GRAY + "Aversion: Necromancy");
        meta.setLore(raceLore);
        choiceItem.setItemMeta(meta);
        aversions.setItem(4, choiceItem);

        choiceItem.setType(Material.ELYTRA);
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Avian");
        raceLore.clear();
        raceLore.add(ChatColor.GRAY + "Aversion: Geomancy");
        meta.setLore(raceLore);
        choiceItem.setItemMeta(meta);
        aversions.setItem(5, choiceItem);

        choiceItem.setType(Material.CHARCOAL);
        meta.setDisplayName(ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "Icaar");
        raceLore.clear();
        raceLore.add(ChatColor.GRAY + "Aversion: Hydromancy");
        meta.setLore(raceLore);
        choiceItem.setItemMeta(meta);
        aversions.setItem(6, choiceItem);

        choiceItem.setType(Material.BLAZE_POWDER);
        meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Specter");
        raceLore.clear();
        raceLore.add(ChatColor.GRAY + "Aversion: Lunimancy");
        meta.setLore(raceLore);
        choiceItem.setItemMeta(meta);
        aversions.setItem(7, choiceItem);

        choiceItem.setType(Material.END_CRYSTAL);
        meta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Ethereal");
        raceLore.clear();
        raceLore.add(ChatColor.GRAY + "Aversion: Umbramancy");
        meta.setLore(raceLore);
        choiceItem.setItemMeta(meta);
        aversions.setItem(8, choiceItem);
    }

    //events

    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {
        Player playerJ = event.getPlayer();
        if (playerJ != null) {
            File playerFile = new File(playerDataFolder, playerJ.getUniqueId().toString() + ".yml");
            if (!playerFile.exists()) {
                try {
                    playerFile.createNewFile();
                } catch (Exception e) {
                    Bukkit.getConsoleSender().sendMessage("Player File Failed");
                }
            }
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getView().getTitle().contains("Are you sure?")) {
            if (event.getCurrentItem() == null) return;
            if (event.getCurrentItem().getItemMeta() == null) return;
            if (event.getCurrentItem().getItemMeta().getDisplayName() == null) return;
            event.setCancelled(true);
            //if(!event.getRawSlot()<event.getView().getTopInventory().getSize()) return;

            this.player = (Player) event.getWhoClicked();
            this.playerFile = new File(playerDataFolder, player.getUniqueId().toString() + ".yml");
            reloadPData();
            if (event.getRawSlot() == 0) {
                Bukkit.getConsoleSender().sendMessage(player.getName());
                if (getPData().contains("DeleteCharacter")) {
                    String delChar = getPData().getString("DeleteCharacter");
                    getPData().set("characters." + delChar, null);
                    player.sendMessage(ChatColor.DARK_RED + delChar + " has been deleted.");
                    getPData().set("DeleteCharacter", null);
                    player.closeInventory();
                    savePData();
                    return;
                }
                player.sendMessage(ChatColor.RED + "Error: No character selected for deletion.");
                player.closeInventory();
                return;
            }
            if (event.getRawSlot() == 8) {
                player.closeInventory();
                getPData().set("DeleteCharacter", null);
                return;
            }
        }
        if (event.getView().getTitle().contains(ChatColor.BOLD + "" + ChatColor.DARK_GREEN + "Choose your Race/Aversion.")) {
            if (event.getCurrentItem() == null) return;
            if (event.getCurrentItem().getItemMeta() == null) return;
            if (event.getCurrentItem().getItemMeta().getDisplayName() == null) return;
            event.setCancelled(true);
            this.player = (Player) event.getWhoClicked();
            this.playerFile = new File(playerDataFolder, player.getUniqueId().toString() + ".yml");
            reloadPData();
            String activeChar = getPData().getString("ActiveCharacter");
            if (!(charName == null)) {
                activeChar = charName;
            }
            if (event.getRawSlot() == 0) {
                getPData().set("characters." + activeChar + ".points.magic.nature.life.aversion", true);
                getPData().set("choosing", null);
                savePData();
                player.closeInventory();
                player.sendMessage(ChatColor.GREEN + "You have chosen: " + ChatColor.GOLD + "Human (Vivimancy Aversion)");
                return;
            }

            if (event.getRawSlot() == 1) {
                getPData().set("characters." + activeChar + ".points.magic.elemental.fire.aversion", true);
                getPData().set("choosing", null);
                savePData();
                player.closeInventory();
                player.sendMessage(ChatColor.GREEN + "You have chosen: " + ChatColor.GREEN + "Kami (Pyromancy Aversion)");
                return;
            }

            if (event.getRawSlot() == 2) {
                getPData().set("characters." + activeChar + ".points.magic.elemental.wind.aversion", true);
                getPData().set("choosing", null);
                savePData();
                player.closeInventory();
                player.sendMessage(ChatColor.GREEN + "You have chosen: " + ChatColor.AQUA + "Merfolk (Aeromancy Aversion)");
                return;
            }

            if (event.getRawSlot() == 3) {
                getPData().set("characters." + activeChar + ".points.magic.nature.ecomancy.aversion", true);
                getPData().set("choosing", null);
                savePData();
                player.closeInventory();
                player.sendMessage(ChatColor.GREEN + "You have chosen: " + ChatColor.DARK_RED + "Demon (Ecomancy Aversion)");
                return;
            }

            if (event.getRawSlot() == 4) {
                getPData().set("characters." + activeChar + ".points.magic.nature.death.aversion", true);
                getPData().set("choosing", null);
                savePData();
                player.closeInventory();
                player.sendMessage(ChatColor.GREEN + "You have chosen: " + ChatColor.DARK_PURPLE + "Moon Elf (Necromancy Aversion)");
                return;
            }
            if (event.getRawSlot() == 5) {
                getPData().set("characters." + activeChar + ".points.magic.elemental.earth.aversion", true);
                getPData().set("choosing", null);
                savePData();
                player.closeInventory();
                player.sendMessage(ChatColor.GREEN + "You have chosen: " + ChatColor.LIGHT_PURPLE + "Avian (Geomancy Aversion)");
                return;
            }
            if (event.getRawSlot() == 6) {
                getPData().set("characters." + activeChar + ".points.magic.elemental.water.aversion", true);
                getPData().set("choosing", null);
                savePData();
                player.closeInventory();
                player.sendMessage(ChatColor.GREEN + "You have chosen: " + ChatColor.DARK_GRAY + "Icaar (Hydromancy Aversion)");
                return;
            }
            if (event.getRawSlot() == 7) {
                getPData().set("characters." + activeChar + ".points.magic.elemental.water.aversion", true);
                getPData().set("choosing", null);
                savePData();
                player.closeInventory();
                player.sendMessage(ChatColor.GREEN + "You have chosen: " + ChatColor.RED + "Specter (Lunimancy Aversion)");
                return;
            }
            if (event.getRawSlot() == 8) {
                getPData().set("characters." + activeChar + ".points.magic.elemental.water.aversion", true);
                getPData().set("choosing", null);
                savePData();
                player.closeInventory();
                player.sendMessage(ChatColor.GREEN + "You have chosen: " + ChatColor.YELLOW + "Ethereal (Umbramancy Aversion)");
                return;
            }
        }
        return;
    }


}

package com.oftx.changedifficulty;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.geysermc.cumulus.form.Form;
import org.geysermc.cumulus.response.SimpleFormResponse;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ChangeDifficulty extends JavaPlugin {

    @Override
    public void onEnable() {
        this.getCommand("chdifficulty").setExecutor(this);
    }

    @Override
    public void onDisable() {
        getLogger().info("ChangeDifficulty 插件已禁用！");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by a player");
            return true;
        }

        if (command.getName().equalsIgnoreCase("chdifficulty")) {
            if (args.length == 0) { // 没有参数：打开表单或对话
                Player player = (Player) sender;
                if (isCommandSenderBedrockPlayer(player)) { // 基岩版玩家：发送表单
                    // 构建表单
                    String title = "更改世界难度";
                    String content = "当前世界: " + player.getWorld().getName() + "\n当前难度: " + player.getWorld().getDifficulty();
                    List<String> buttonsLabel = new ArrayList<String>(Arrays.asList("简单", "普通", "困难"));
                    Consumer<SimpleFormResponse> responseConsumer = response -> {
                        int clickedButtonIndex = response.clickedButtonId();
                        World world = player.getWorld();
                        if (clickedButtonIndex == 0) {
                            world.setDifficulty(Difficulty.EASY);
                        } else if (clickedButtonIndex == 1) {
                            world.setDifficulty(Difficulty.NORMAL);
                        } else if (clickedButtonIndex == 2) {
                            world.setDifficulty(Difficulty.HARD);
                        }
                        Bukkit.broadcastMessage("世界 " + world.getName() + " 的难度被 " + player.getName() + " 修改为 " + player.getWorld().getDifficulty());
                    };
                    Form form = buildSimpleForm(title, content, buttonsLabel, responseConsumer);
                    if (form == null) {
                        player.sendMessage("表单创建失败");
                        return true;
                    }

                    // 发送表单
                    FloodgateApi.getInstance().sendForm(player.getUniqueId(), form);

                    return true;
                } else { // Java版玩家：发送对话
                    // 构建对话内容
                    String message = "请选择世界难度:\n0. 和平\n1. 简单\n2. 普通\n3. 困难";
                    player.sendMessage(message);

                    // 监听玩家的聊天输入
                    Bukkit.getServer().getPluginManager().registerEvents(new Listener() {
                        @EventHandler
                        public void onPlayerChat(AsyncPlayerChatEvent event) {
                            if (event.getPlayer().equals(player)) {
                                String message = event.getMessage();
                                World world = player.getWorld();
                                if (message.equals("0")) {
                                    world.setDifficulty(Difficulty.PEACEFUL);
                                } else if (message.equals("1")) {
                                    world.setDifficulty(Difficulty.EASY);
                                } else if (message.equals("2")) {
                                    world.setDifficulty(Difficulty.NORMAL);
                                } else if (message.equals("3")) {
                                    world.setDifficulty(Difficulty.HARD);
                                } else {
                                    player.sendMessage("无效选项");
                                    event.setCancelled(true);
                                    HandlerList.unregisterAll(this);
                                    return;
                                }
                                Bukkit.broadcastMessage("世界 " + world.getName() + " 的难度被 " + player.getName() + " 修改为 " + player.getWorld().getDifficulty());
                                event.setCancelled(true);
                                HandlerList.unregisterAll(this);
                            }
                        }
                    }, this);
                    return true;
                }
            } else if (args.length == 1) { // 一个参数
                String arg = args[0];
                Player player = (Player) sender;
                World world = player.getWorld();
                switch (arg) {
                    case "0":
                        world.setDifficulty(Difficulty.PEACEFUL);
                        break;
                    case "1":
                        world.setDifficulty(Difficulty.EASY);
                        break;
                    case "2":
                        world.setDifficulty(Difficulty.NORMAL);
                        break;
                    case "3":
                        world.setDifficulty(Difficulty.HARD);
                        break;
                    default:
                        player.sendMessage("无效参数");
                        return true;
                }
                Bukkit.broadcastMessage("世界 " + world.getName() + " 的难度被 " + player.getName() + " 修改为 " + player.getWorld().getDifficulty());
                return true;
            }
        }
        return false;
    }

    private boolean isCommandSenderBedrockPlayer(Player player) {
        if (!FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) {
            return false;
        }
        return true;
    }

    private Form buildSimpleForm(String title, String content, List<String> buttonsLabel, Consumer<SimpleFormResponse> resultHandler) {
        if (title == null || title.isEmpty()) return null;
        if (content == null || content.isEmpty()) return null;

        org.geysermc.cumulus.form.SimpleForm.Builder formBuilder = org.geysermc.cumulus.form.SimpleForm.builder()
                .title(title)
                .content(content);

        for (String buttonLabel : buttonsLabel) {
            formBuilder.button(buttonLabel);
        }

        try {
            formBuilder.validResultHandler(resultHandler);
        } catch (Exception e) {
            e.printStackTrace();
            getLogger().warning("以上错误在处理表单结果时发生(validResultHandler)");
            return null;
        }

        return formBuilder.build();
    }
}

/*
 * @Author: hiddenSharp429 z404878860@163.com
 * @Date: 2024-10-29 22:46:23
 * @LastEditors: hiddenSharp429 z404878860@163.com
 * @LastEditTime: 2024-11-15 21:40:12
 */
package com.example.demo.service.game;

import com.example.demo.model.game.GameRoom;
import com.example.demo.model.game.GameStatus;
import com.example.demo.entity.enums.TextLanguage;
import com.example.demo.entity.enums.TextCategory;
import com.example.demo.model.game.GameMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.example.demo.service.user.UserService;
import com.example.demo.entity.User;

@Service
public class GameRoomService {
    private final Map<String, GameRoom> rooms = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(GameRoomService.class);
    private final SimpMessagingTemplate messagingTemplate;
    private final GameTextService gameTextService;
    private final UserService userService;
    private static final int MAX_PLAYERS = 2;

    @Autowired
    public GameRoomService(SimpMessagingTemplate messagingTemplate, GameTextService gameTextService, UserService userService) {
        this.messagingTemplate = messagingTemplate;
        this.gameTextService = gameTextService;
        this.userService = userService;
    }

    public GameRoom createRoom(String roomId, TextLanguage language, TextCategory category, String difficulty) {
        GameRoom room = new GameRoom(roomId);
        
        // 设置房间的语言、类型和难度属性
        room.setLanguage(language);
        room.setCategory(category);
        room.setDifficulty(difficulty);
        
        // 获取并设置目标文本
        String targetText = gameTextService.getRandomText(language, category, difficulty);
        room.setTargetText(targetText);
        
        // 存储房间
        rooms.put(roomId, room);
        
        return room;
    }

    // 添加一个使用默认参数的重载方法
    public GameRoom createRoom(String roomId) {
        return createRoom(
            roomId,
            TextLanguage.ENGLISH,  // 默认使用英语
            TextCategory.DAILY_CHAT,  // 默认使用日常对话
            "EASY"  // 默认使用简单难度
        );
    }

    public GameRoom getRoom(String roomId) {
        return rooms.get(roomId);
    }

    public GameRoom joinRoom(String roomId, String playerId, String playerName, 
                            TextLanguage language, TextCategory category, String difficulty) {
        GameRoom room = rooms.get(roomId);
        if (room == null) {
            // 使用传入的参数创建房间
            room = createRoom(roomId, language, category, difficulty);
        }
        
        if (addPlayer(room, playerId, playerName)) {
            if (isRoomFull(room)) {
                setGameStatus(room, GameStatus.READY);
            }
        }
        
        return room;
    }

    // 添加一个使用默认参数的重载方法
    public GameRoom joinRoom(String roomId, String playerId, String playerName) {
        return joinRoom(
            roomId,
            playerId,
            playerName,
            TextLanguage.ENGLISH,  // 默认使用英语
            TextCategory.DAILY_CHAT,  // 默认使用日常对话
            "EASY"  // 默认使用简单难度
        );
    }

    public void leaveRoom(String roomId, String playerId, String playerName) {
        GameRoom room = rooms.get(roomId);
        if (room != null) {
            if (removePlayer(room, playerId, playerName)) {
                rooms.remove(roomId);
            } else {
                setGameStatus(room, GameStatus.WAITING);
            }
        }
    }

    public boolean roomExists(String roomId) {
        return rooms.containsKey(roomId);
    }

    public void updateGameStatus(String roomId, GameStatus status) {
        GameRoom room = rooms.get(roomId);
        if (room != null) {
            setGameStatus(room, status);
        }
    }

    public void setGameStatus(GameRoom room, GameStatus status) {
        room.setStatus(status);
        if (status == GameStatus.PLAYING) {
            room.setStartTime(System.currentTimeMillis());
        }
    }

    public boolean addPlayer(GameRoom room, String playerId, String playerName) {
        if (!isRoomFull(room)) {
            room.getPlayersId().add(playerId);
            room.getPlayersName().add(playerName);
            return true;
        }
        return false;
    }

    public boolean removePlayer(GameRoom room, String playerId, String playerName) {
        room.getPlayersId().remove(playerId);
        room.getPlayersName().remove(playerName);
        return room.getPlayersId().isEmpty();
    }

    public boolean isRoomFull(GameRoom room) {
        return room.getPlayersId().size() >= MAX_PLAYERS;
    }

    public boolean isRoomEmpty(GameRoom room) {
        return room.getPlayersId().isEmpty();
    }

    public boolean isPlayerHost(GameRoom room, String playerId) {
        return !room.getPlayersId().isEmpty() && 
               room.getPlayersId().iterator().next().equals(playerId);
    }

    public boolean hasPlayer(GameRoom room, String playerId) {
        return room.getPlayersId().contains(playerId);
    }

    public GameMessage getRoomInfo(String roomId, String requestPlayerId, String requestPlayerName) {
        GameRoom room = getRoom(roomId);
        if (room == null) {
            return null;
        }

        // 找出对手信息
        String opponentId = room.getPlayersId().stream()
            .filter(id -> !id.equals(requestPlayerId))
            .findFirst()
            .orElse(null);
            
        String opponentName = opponentId != null ? 
            room.getPlayersName().stream()
                .filter(name -> !name.equals(requestPlayerName))
                .findFirst()
                .orElse(null) : null;

        // 构建房间信息消息
        GameMessage roomInfo = new GameMessage();
        roomInfo.setType("GAME_INFO");
        roomInfo.setRoomId(roomId);
        roomInfo.setPlayerId(requestPlayerId);
        roomInfo.setPlayerName(requestPlayerName);
        
        // 获取请求玩家的头像
        String playerAvatar = userService.getUserById(Long.parseLong(requestPlayerId))
                .map(User::getImgSrc)
                .orElse("https://api.dicebear.com/7.x/avataaars/svg?seed=" + requestPlayerName);
        roomInfo.setPlayerAvatar(playerAvatar);
        
        roomInfo.setOpponentId(opponentId);
        roomInfo.setOpponentName(opponentName);
        
        // 获取对手的头像
        String opponentAvatar = opponentId != null ? 
            userService.getUserById(Long.parseLong(opponentId))
                    .map(User::getImgSrc)
                    .orElse("https://api.dicebear.com/7.x/avataaars/svg?seed=" + opponentName) 
            : null;
        roomInfo.setOpponentAvatar(opponentAvatar);
        
        roomInfo.setLanguage(room.getLanguage().toString());
        roomInfo.setCategory(room.getCategory().toString());
        roomInfo.setDifficulty(room.getDifficulty());
        roomInfo.setRoomStatus(room.getStatus().toString());
        roomInfo.setPlayersCount(room.getPlayersId().size());
        roomInfo.setTargetText(room.getTargetText());
        roomInfo.setStartTime(room.getStartTime());
        roomInfo.setTimestamp(System.currentTimeMillis());
        roomInfo.setIsHost(room.getHostId().equals(requestPlayerId));

        return roomInfo;
    }

    public void playerReady(String roomId, String playerId, String playerName, Boolean isReady) {
        GameRoom room = getRoom(roomId);
        if (room != null) {
            // 根据isReady参数设置房间状态
            GameStatus newStatus = isReady ? GameStatus.READY : GameStatus.WAITING;
            setGameStatus(room, newStatus);
            
            // 找到另一个玩家的ID
            String otherPlayerId = room.getPlayersId().stream()
                .filter(id -> !id.equals(playerId))
                .findFirst()
                .orElse(null);
            
            if (otherPlayerId != null) {
                // 通知另一个玩家
                messagingTemplate.convertAndSend(
                    "/queue/room/" + otherPlayerId + "/info",
                    new GameMessage() {{
                        setType("PLAYER_READY");
                        setPlayerId(playerId);          // 准备的玩家ID
                        setPlayerName(playerName);      // 准备的玩家名称
                        setRoomId(roomId);
                        setIsReady(isReady);  // 设置准备状态
                        setRoomStatus(newStatus.toString());
                        setTimestamp(System.currentTimeMillis());
                    }}
                );
            }
        }
    }
}
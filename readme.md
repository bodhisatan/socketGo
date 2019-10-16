# 基于Socket的五子棋AI博弈系统

## 关键词

* JavaFx
* Socket
* 多线程
* JSON

## 概述

* 用JavaFx设计GUI，Server端和Client端分别有独立GUI，三个基于不同的AI算法的Client相互独立，连接上Server后循环博弈

* 接入3个客户端之后，用户点击“开始博弈”，Server端采用循环赛的方式，每次从三个socket连接中选出两个放入比赛池进行博弈
  * 对于每次博弈，Server端会新开两个线程，对应比赛中的两个socket client端
  * 博弈开始时，Server端向两个比赛中的client端发送`Start`指令，并附上随机选出的先手玩家姓名，client端接收到`Start`指令后，判断自己是不是先手玩家，如果是，执黑子，并落第一步棋，如果不是，执白子
  * 博弈进行过程中，client端对于自己的每一步落子操作，封装成JSON格式（`{name, x, y, isEnd}`)发给Server端，Server端接收到之后，将其转发，通知另一个client同步棋盘并落子
  * JSON中的`isEnd`字段表示游戏是否已经决出胜负，当Server端接收到这条JSON时，将这条信息转发给client通知其清理资源等待再次开局，同时此刻的Server端进程结束，清理棋盘，进入下一盘比赛
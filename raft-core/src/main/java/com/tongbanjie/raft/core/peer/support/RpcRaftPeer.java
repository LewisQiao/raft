package com.tongbanjie.raft.core.peer.support;

import com.alibaba.fastjson.JSON;
import com.tongbanjie.raft.core.engine.RaftEngine;
import com.tongbanjie.raft.core.enums.RemotingCommandState;
import com.tongbanjie.raft.core.enums.RemotingCommandType;
import com.tongbanjie.raft.core.exception.RaftException;
import com.tongbanjie.raft.core.peer.RaftPeer;
import com.tongbanjie.raft.core.protocol.AppendEntriesRequest;
import com.tongbanjie.raft.core.protocol.AppendEntriesResponse;
import com.tongbanjie.raft.core.protocol.ElectionRequest;
import com.tongbanjie.raft.core.protocol.ElectionResponse;
import com.tongbanjie.raft.core.remoting.RemotingClient;
import com.tongbanjie.raft.core.remoting.RemotingCommand;
import com.tongbanjie.raft.core.remoting.RemotingServer;
import com.tongbanjie.raft.core.remoting.builder.RemotingClientBuilder;
import com.tongbanjie.raft.core.remoting.builder.RemotingServerBuilder;
import com.tongbanjie.raft.core.remoting.support.netty.NettyClient;
import com.tongbanjie.raft.core.remoting.support.netty.NettyServer;
import com.tongbanjie.raft.core.remoting.support.netty.RemotingCommandProcessor;
import com.tongbanjie.raft.core.util.RequestIdGenerator;

import java.util.Random;

/***
 * 基于rpc方式的 raft peer
 * @author banxia
 * @date 2017-11-15 17:17:52
 */
public class RpcRaftPeer implements RaftPeer {


    private Random random = new Random();

    private RaftEngine raftEngine;


    private RemotingServer remotingServer;


    private RemotingClient remotingClient;


    private RemotingCommandProcessor remotingCommandProcessor = new RemotingCommandProcessor(this);

    private String id;

    private String host;

    private int port;

    public RpcRaftPeer(String id) {

        this.id = id;
        String[] split = this.id.split(":");
        this.host = split[0];
        this.port = Integer.valueOf(split[1]);
        this.remotingClient = new RemotingClientBuilder().host(host).port(port).requestTimeout(3000).builder();
    }


    public void setRemotingServer(RemotingServer remotingServer) {
        this.remotingServer = remotingServer;
    }


    public void registerServer() {

        if (this.remotingServer == null) {

            this.remotingServer = new RemotingServerBuilder().host(host)
                    .port(port).remotingCommandProcessor(this.remotingCommandProcessor).builder();

        }
    }

    /**
     * 取消注册服务
     */
    public void unregisterServer() {
        if (this.remotingServer != null && !this.remotingServer.isClosed()) {
            this.remotingServer.close();
        }
    }


    public RaftEngine getRaftEngine() {
        return raftEngine;
    }

    public void setRaftEngine(RaftEngine raftEngine) {
        this.raftEngine = raftEngine;
    }


    public String getId() {
        return id;
    }

    public RemotingClient getRemotingClient() {


        if (remotingClient == null || remotingClient.isClosed()) {
            remotingClient = new NettyClient();
            String[] split = this.id.split(":");
            String host = split[0];
            int port = Integer.valueOf(split[1]);
            boolean open = remotingClient.open();
            if (!open) {
                throw new RaftException("the netty client open fail");
            }
        }
        return this.remotingClient;
    }

    public RemotingServer getRemotingServer() {
        return this.remotingServer;
    }

    /**
     * 发起投票选举
     *
     * @param request 投票选举请求体
     * @return 投票选举响应实体
     */
    public ElectionResponse electionVote(ElectionRequest request) {

        RemotingClient remotingClient = this.getRemotingClient();

        long requestId = RequestIdGenerator.getRequestId();
        RemotingCommand command = new RemotingCommand();
        command.setRequestId(requestId);
        command.setCommandType(RemotingCommandType.ELECTION.getValue());
        command.setBody(JSON.toJSONString(request));
        command.setState(RemotingCommandState.SUCCESS.getValue());
        RemotingCommand remotingCommand = remotingClient.request(command);

        if (remotingCommand != null && remotingCommand.getState() == RemotingCommandState.SUCCESS.getValue()) {

            return JSON.parseObject(remotingCommand.getBody(), ElectionResponse.class);
        } else {

            throw new RaftException("election vote request fail ");

        }


    }

    public AppendEntriesResponse appendEntries(AppendEntriesRequest request) {

        long requestId = RequestIdGenerator.getRequestId();
        RemotingCommand command = new RemotingCommand();
        command.setRequestId(requestId);
        command.setCommandType(RemotingCommandType.APPEND.getValue());
        command.setBody(JSON.toJSONString(request));
        command.setState(RemotingCommandState.SUCCESS.getValue());
        RemotingCommand remotingCommand = remotingClient.request(command);
        if (remotingCommand != null && remotingCommand.getState() == RemotingCommandState.SUCCESS.getValue()) {

            return JSON.parseObject(remotingCommand.getBody(), AppendEntriesResponse.class);

        } else {

            throw new RaftException("append entries request fail");

        }
    }

    /**
     * @param electionRequest
     * @return
     */
    public ElectionResponse electionVoteHandler(ElectionRequest electionRequest) {


        return this.raftEngine.electionVoteHandler(electionRequest);

    }

    public AppendEntriesResponse appendEntriesHandler(AppendEntriesRequest request) {

        return this.raftEngine.appendEntriesHandler(request);
    }


}

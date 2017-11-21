package com.tongbanjie.raft.core.remoting;

import com.tongbanjie.raft.core.enums.RemotingChannelState;

/***
 *
 * @author banxia
 * @date 2017-11-21 14:14:37
 */
public abstract class AbstractRemotingClient implements RemotingChannel {

    protected RemotingChannelState state = RemotingChannelState.UNINIT;

}

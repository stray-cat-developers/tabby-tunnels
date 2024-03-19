package org.straycats.tabbytunnels.utils

import org.straycats.tabbytunnels.common.Replies
import org.straycats.tabbytunnels.common.Reply

fun <T> List<T>.toReplies(): Replies<T> = Replies(this)
fun <T> T.toReply(): Reply<T> = Reply(this)

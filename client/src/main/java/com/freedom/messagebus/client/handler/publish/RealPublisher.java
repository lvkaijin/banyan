package com.freedom.messagebus.client.handler.publish;

import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.handler.AbstractHandler;
import com.freedom.messagebus.client.handler.IHandlerChain;
import com.freedom.messagebus.client.model.HandlerModel;
import com.freedom.messagebus.common.CONSTS;
import com.freedom.messagebus.common.message.Message;
import com.freedom.messagebus.interactor.message.IMessageBodyProcessor;
import com.freedom.messagebus.interactor.message.MessageBodyProcessorFactory;
import com.freedom.messagebus.interactor.message.MessageHeaderProcessor;
import com.freedom.messagebus.interactor.proxy.ProxyProducer;
import com.rabbitmq.client.AMQP;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class RealPublisher extends AbstractHandler {

    private static final Log logger = LogFactory.getLog(RealPublisher.class);

    @Override
    public void init(@NotNull HandlerModel handlerModel) {
        super.init(handlerModel);
    }

    @Override
    public void handle(@NotNull MessageContext context, @NotNull IHandlerChain chain) {
        try {
            for (Message msg : context.getMessages()) {
                IMessageBodyProcessor msgBodyProcessor = MessageBodyProcessorFactory.createMsgBodyProcessor(msg.getMessageType());
                byte[] msgBody = msgBodyProcessor.box(msg.getMessageBody());
                AMQP.BasicProperties properties = MessageHeaderProcessor.box(msg.getMessageHeader());
                ProxyProducer.produce(CONSTS.PROXY_EXCHANGE_NAME,
                                      context.getChannel(),
                                      CONSTS.PUBSUB_ROUTING_KEY,
                                      msgBody,
                                      properties);
            }

            chain.handle(context);
        } catch (IOException e) {
            logger.error("[handle] occurs a IOException : " + e.getMessage());
        } finally {
            context.getDestroyer().destroy(context.getChannel());
        }
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}

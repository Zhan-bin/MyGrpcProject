package org.examplecode;

import io.grpc.NameResolver;
import io.grpc.NameResolverProvider;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;

/**
 * @Author tanyong
 * @Version LoadBalanceNameResolverProvider v1.0.0 2024/4/3 9:56 $$
 */
public class LoadBalanceNameResolverProvider extends NameResolverProvider {
    private final List<InetSocketAddress> socketAddressList;

    public LoadBalanceNameResolverProvider(List<InetSocketAddress> socketAddressList) {
        this.socketAddressList = socketAddressList;
    }

    @Override
    protected boolean isAvailable() {
        return true;
    }

    @Override
    protected int priority() {
        return 5;
    }

    @Override
    public NameResolver newNameResolver(URI targetUri, NameResolver.Args args) {
        return new LoadBalanceNameResolver(socketAddressList, HelloWordConstants.SERVICE_NAME);
    }

    @Override
    public String getDefaultScheme() {
        return HelloWordConstants.SCHEME;
    }
}



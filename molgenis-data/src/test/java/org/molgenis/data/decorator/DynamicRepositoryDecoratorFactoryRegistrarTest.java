package org.molgenis.data.decorator;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.context.ApplicationContext;

class DynamicRepositoryDecoratorFactoryRegistrarTest extends AbstractMockitoTest {
  @Mock DynamicRepositoryDecoratorRegistry repositoryDecoratorRegistry;
  @Mock DynamicRepositoryDecoratorFactory decoratorFactory1;
  @Mock DynamicRepositoryDecoratorFactory decoratorFactory2;
  @Mock ApplicationContext context;

  @Test
  void testRegister() {
    Map<String, DynamicRepositoryDecoratorFactory> map = new HashMap<>();
    map.put("decoratorFactory1", decoratorFactory1);
    map.put("decoratorFactory2", decoratorFactory2);

    when(context.getBeansOfType(DynamicRepositoryDecoratorFactory.class)).thenReturn(map);

    DynamicRepositoryDecoratorFactoryRegistrar dynamicRepositoryDecoratorFactoryRegistrar =
        new DynamicRepositoryDecoratorFactoryRegistrar(repositoryDecoratorRegistry);
    dynamicRepositoryDecoratorFactoryRegistrar.register(context);

    verify(repositoryDecoratorRegistry).addFactory(decoratorFactory1);
    verify(repositoryDecoratorRegistry).addFactory(decoratorFactory2);
  }
}

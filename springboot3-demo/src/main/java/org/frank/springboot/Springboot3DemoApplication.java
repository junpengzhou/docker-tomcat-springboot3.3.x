package org.frank.springboot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Springboot3示例工程
 *
 * @author FrankZhou
 * @since 2026-03-28 16:39:48
 * @version 1.0.0
 */
@SpringBootApplication
@Slf4j
public class Springboot3DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(Springboot3DemoApplication.class, args);
        log.info("""
                
                      ::::::::::       :::::::::           :::        ::::    :::       :::    :::\s
                     :+:              :+:    :+:        :+: :+:      :+:+:   :+:       :+:   :+:  \s
                    +:+              +:+    +:+       +:+   +:+     :+:+:+  +:+       +:+  +:+    \s
                   :#::+::#         +#++:++#:       +#++:++#++:    +#+ +:+ +#+       +#++:++      \s
                  +#+              +#+    +#+      +#+     +#+    +#+  +#+#+#       +#+  +#+      \s
                 #+#              #+#    #+#      #+#     #+#    #+#   #+#+#       #+#   #+#      \s
                ###              ###    ###      ###     ###    ###    ####       ###    ###      \s
                
                Hello, I am frank, Welcome to springboot3 environment!\s
                
                """);
    }
}

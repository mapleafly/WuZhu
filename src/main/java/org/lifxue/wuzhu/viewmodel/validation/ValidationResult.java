/*
 * Copyright 2019 xuelf.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.lifxue.wuzhu.viewmodel.validation;

/**
 * 验证结果封装类，用于ViewModel中的输入验证
 *
 * @author xuelf
 * @date 2025/05/03
 */
public class ValidationResult {

    private final boolean valid;
    private final String message;

    private ValidationResult(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }

    /**
     * 创建成功的验证结果
     *
     * @return 验证成功结果
     */
    public static ValidationResult success() {
        return new ValidationResult(true, null);
    }

    /**
     * 创建失败的验证结果
     *
     * @param message 错误消息
     * @return 验证失败结果
     */
    public static ValidationResult error(String message) {
        return new ValidationResult(false, message);
    }

    /**
     * @return 是否验证通过
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * @return 验证消息（成功时为null，失败时为错误消息）
     */
    public String getMessage() {
        return message;
    }
}

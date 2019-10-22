package org.wso2.carbon.identity.oauth2.device.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth.tokenprocessor.HashingPersistenceProcessor;
import org.wso2.carbon.identity.oauth.tokenprocessor.PlainTextPersistenceProcessor;
import org.wso2.carbon.identity.oauth.tokenprocessor.TokenPersistenceProcessor;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;

import java.sql.Connection;

/*
NOTE
This is the very first step of moving to simplified architecture for token persistence. New set of DAO classes  for
each purpose  and factory class to get instance of each DAO classes were introduced  during  this step. Further methods
 on org.wso2.carbon.identity.oauth2.org.wso2.identity.oauth2.device.dao.TokenMgtDAO were distributed among new set of classes, each of these method
 need to be reviewed  and refactored  during next step.
 */
abstract class AbstractOAuthDAO {

    private static final Log log = LogFactory.getLog(AbstractOAuthDAO.class);

    private static final boolean DEFAULT_PERSIST_ENABLED = true;
    // These config properties are defined in identity.xml
    private static final String OAUTH_TOKEN_PERSISTENCE_ENABLE = "OAuth.TokenPersistence.Enable";
    // We read from these properties for the sake of backward compatibility
    private static final String FRAMEWORK_PERSISTENCE_ENABLE = "JDBCPersistenceManager.SessionDataPersist.Enable";

    protected static final String UTC = "UTC";
    protected static final String AUTHZ_USER = "AUTHZ_USER";
    protected static final String LOWER_AUTHZ_USER = "LOWER(AUTHZ_USER)";

    private TokenPersistenceProcessor persistenceProcessor, hashingPersistenceProcessor;

    public AbstractOAuthDAO() {

        persistenceProcessor = createPersistenceProcessor();
        hashingPersistenceProcessor = new HashingPersistenceProcessor();

    }

    protected TokenPersistenceProcessor getPersistenceProcessor() {

        return persistenceProcessor;
    }

    /**
     * Method to get HashingPersistenceProcessor instance
     *
     * @return an instance of HashingPersistenceProcessor
     */
    protected TokenPersistenceProcessor getHashingPersistenceProcessor() {

        return hashingPersistenceProcessor;
    }

    protected TokenPersistenceProcessor createPersistenceProcessor() {

        try {
            return OAuthServerConfiguration.getInstance().getPersistenceProcessor();
        } catch (IdentityOAuth2Exception e) {
            log.error("Error retrieving TokenPersistenceProcessor. Defaulting to PlainTextProcessor", e);
            return new PlainTextPersistenceProcessor();
        }
    }

    protected boolean isPersistenceEnabled() {

        if (IdentityUtil.getProperty(OAUTH_TOKEN_PERSISTENCE_ENABLE) != null) {
            return Boolean.parseBoolean(IdentityUtil.getProperty(OAUTH_TOKEN_PERSISTENCE_ENABLE));
        } else if (IdentityUtil.getProperty(FRAMEWORK_PERSISTENCE_ENABLE) != null) {
            return Boolean.parseBoolean(IdentityUtil.getProperty(FRAMEWORK_PERSISTENCE_ENABLE));
        }
        return DEFAULT_PERSIST_ENABLED;
    }

    protected Connection getConnection() {

        return IdentityDatabaseUtil.getDBConnection();
    }

}

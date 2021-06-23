#!/bin/sh

KEYSTORE_DOMAIN="${KEYSTORE_DOMAIN:-darklimericks.com}"
KEYSTORE_PASS="${KEYSTORE_PASS:-hunter22}"

# Create keystore
echo "Refreshing '~/ssl/$KEYSTORE_DOMAIN.keystore'"
openssl pkcs12 -export \
    -in /etc/letsencrypt/live/$KEYSTORE_DOMAIN/cert.pem \
    -inkey /etc/letsencrypt/live/$KEYSTORE_DOMAIN/privkey.pem \
    -out /tmp/$KEYSTORE_DOMAIN.p12 \
    -name $KEYSTORE_DOMAIN \
    -CAfile /etc/letsencrypt/live/$KEYSTORE_DOMAIN/fullchain.pem \
    -caname "Let's Encrypt Authority X3" \
    -password pass:$KEYSTORE_PASS
keytool -importkeystore \
    -deststorepass $KEYSTORE_PASS \
    -destkeypass $KEYSTORE_PASS \
    -deststoretype pkcs12 \
    -srckeystore /tmp/$KEYSTORE_DOMAIN.p12 \
    -srcstoretype PKCS12 \
    -srcstorepass $KEYSTORE_PASS \
    -destkeystore /tmp/$KEYSTORE_DOMAIN.keystore \
    -alias $KEYSTORE_DOMAIN
# Move certificates to other servers
echo "Copy '~/ssl/$KEYSTORE_DOMAIN.keystore' to cluster servers"
cp /tmp/$KEYSTORE_DOMAIN.keystore /root/ssl/$KEYSTORE_DOMAIN.keystore

# Create truststore
echo "Refreshing '~/ssl/theirdomain.be.keystore'"
rm theirdomain.be.keystore
openssl s_client -connect theirdomain.be:443 -showcerts </dev/null 2>/dev/null|openssl x509 -outform DER >theirdomain.der
openssl x509 -inform der -in theirdomain.der -out theirdomain.pem
keytool -import \
    -alias theirdomain \
    -keystore theirdomain.be.keystore \
    -file ./theirdomain.pem \
    -storepass theirdomain \
    -noprompt
echo "Copy '~/ssl/theirdomain.be.keystore' to cluster servers"
cp theirdomain.be.keystore /home/admin_jworks/ssl/
sudo scp ssl/theirdomain.be.keystore cc-backend-node-02:/home/admin_jworks/ssl/
sudo scp ssl/theirdomain.be.keystore cc-frontend-node-01:/home/admin_jworks/ssl/

FROM httpd:2.4

COPY proxy.conf /usr/local/apache2/conf/extra/proxy.conf

COPY certs/ /usr/local/apache2/conf/certs/

#COPY html/ /usr/local/apache2/htdocs/

RUN echo "Include conf/extra/proxy.conf" >> /usr/local/apache2/conf/httpd.conf

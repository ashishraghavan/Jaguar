package com.jaguar.om.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jaguar.om.IAccount;
import com.jaguar.om.IApplication;
import com.jaguar.om.IApplicationRole;
import com.jaguar.om.common.Utils;
import com.jaguar.om.enums.ApplicationType;
import org.apache.log4j.Logger;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.net.URI;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "jaguar_application",uniqueConstraints = {@UniqueConstraint(columnNames = {"account_id","name"})})
@AttributeOverride(name = "id",column = @Column(name = "application_id"))
public class Application extends CommonObject implements IApplication{

    private static final Logger appObjModelLogger = Logger.getLogger(Application.class.getSimpleName());

    @ManyToOne(targetEntity = Account.class,optional = false)
    @JoinColumn(name = "account_id",insertable = true,updatable = true,nullable = false)
    @ForeignKey(name = "fk_application_account_id")
    private IAccount account;

    @Column(name = "name",insertable = true,updatable = true,nullable = false,length = 100)
    private String name;

    @Column(name = "redirect_uri",insertable = true,updatable = true,nullable = false)
    private String redirectUri;

    @Column(name = "client_id",unique = true,insertable = true,updatable = false,nullable = false)
    private Integer clientId;


    @Column(name = "client_secret",insertable = true,updatable = false,nullable = false)
    private String clientSecret;

    @Column(name = "version_code",nullable = false,insertable = true,updatable = true,length = 150)
    private String versionCode;

    @Column(name = "package_name",nullable = false,insertable = true,updatable = true,length = 150)
    private String packageName;

    @Column(name = "app_type",nullable = false,insertable = true,updatable = true,length = 30)
    @Enumerated(EnumType.STRING)
    private ApplicationType applicationType;

    @Column(name = "login_page",nullable = true,insertable = true,updatable = true)
    private String loginPage;

    @JsonIgnore
    @OneToMany(mappedBy = "application",targetEntity = ApplicationRole.class,fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @org.hibernate.annotations.LazyCollection(LazyCollectionOption.FALSE)
    private Set<IApplicationRole> applicationRoles;


    public Application(){
        super();
    }

    //To be used when trying to fetch an application with a clientid.
    public Application(final Integer clientId) {
        this();
        this.clientId = clientId;
    }

    public Application(final IAccount account, final String name, final String redirectUri, ApplicationType applicationType,final String packageName) {
        this(account,name);
        this.clientId = Utils.generateApplicationKey();
        this.clientSecret = UUID.randomUUID().toString();
        this.redirectUri = redirectUri;
        this.applicationType = applicationType;
        this.packageName = packageName;
    }

    public Application(final IAccount account, final String name) {
        this();
        this.account = account;
        this.name = name;
    }

    public Application(final IAccount account,Integer clientId) {
        this(clientId);
        this.account = account;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getClientId() {
        return this.clientId;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return this.clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getRedirectURI() {
        return this.redirectUri;
    }

    public void setRedirectURI(String redirectURI) {
        this.redirectUri = redirectURI;
    }

    public Set<IApplicationRole> getRoles() {
        return this.applicationRoles;
    }

    public IAccount getAccount() {
        return this.account;
    }

    public void setAccount(IAccount account) {
        this.account = account;
    }

    public String getVersionCode() {
        return this.versionCode;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public ApplicationType getApplicationType() {
        return this.applicationType;
    }

    public void setApplicationType(ApplicationType applicationType) {
        this.applicationType = applicationType;
    }

    @Override
    public void setLoginPage(String uri) {
        //Verify that this is a correct URI
        final URI formedUri = URI.create(uri);
        appObjModelLogger.info("The URI is formed correctly");
        this.loginPage = uri;
    }

    @Override
    public String getLoginPage() {
        return this.loginPage;
    }
}

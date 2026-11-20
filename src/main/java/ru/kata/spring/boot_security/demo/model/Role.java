package ru.kata.spring.boot_security.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.security.core.GrantedAuthority;

@Entity
@Table(name = "roles", schema = "admin_panel")
public class Role implements GrantedAuthority {
    @Id
    @Column(name = "role_name", length = 50)
    private String roleName;

    protected Role() { }
    public Role(String roleName) { this.roleName = roleName; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    @Override public String getAuthority() { return getRoleName(); }
}

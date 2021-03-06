package com.baeldung.rxjava.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.davidmoten.rx.jdbc.ConnectionProvider;
import com.github.davidmoten.rx.jdbc.Database;

import rx.Observable;

public class AutomapInterfaceTest {

    ConnectionProvider connectionProvider = Connector.connectionProvider;
    Database db = Database.from(connectionProvider);

    Observable<Integer> create = null;
    Observable<Integer> insert1, insert2 = null;

    @Before
    public void setup() {
        create = db.update("CREATE TABLE IF NOT EXISTS EMPLOYEE(id int primary key, name varchar(255))")
          .count();
        insert1 = db.update("INSERT INTO EMPLOYEE(id, name) VALUES(1, 'Alan')")
          .dependsOn(create)
          .count();
        insert2 = db.update("INSERT INTO EMPLOYEE(id, name) VALUES(2, 'Sarah')")
          .dependsOn(create)
          .count();
    }

    @Test
    public void whenSelectFromTableAndAutomap_thenCorrect() {
        List<Employee> employees = db.select("select id, name from EMPLOYEE")
          .dependsOn(create)
          .dependsOn(insert1)
          .dependsOn(insert2)
          .autoMap(Employee.class)
          .toList()
          .toBlocking()
          .single();

        assertThat(employees.get(0)
          .id()).isEqualTo(1);
        assertThat(employees.get(0)
          .name()).isEqualTo("Alan");
        assertThat(employees.get(1)
          .id()).isEqualTo(2);
        assertThat(employees.get(1)
          .name()).isEqualTo("Sarah");
    }

    @After
    public void close() {
        db.update("DROP TABLE EMPLOYEE")
            .dependsOn(create);
        connectionProvider.close();
    }

}

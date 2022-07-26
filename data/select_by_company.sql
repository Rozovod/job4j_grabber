CREATE TABLE company
(
    id integer NOT NULL,
    name character varying,
    CONSTRAINT company_pkey PRIMARY KEY (id)
);

CREATE TABLE person
(
    id integer NOT NULL,
    name character varying,
    company_id integer references company(id),
    CONSTRAINT person_pkey PRIMARY KEY (id)
);

insert into company(id, name) values(1, 'Gazprom'), (2, 'Yandex'), (3, 'Apple'),
(4, 'Amazon'), (5, 'Google');

insert into person(id, name, company_id) 
values(1, 'Ivan', 1), (2, 'Stepan', 1), (3, 'Vladimir', 1), 
(4, 'Sergey', 2), (5, 'Valentina', 2), (6, 'Stanislav', 2), 
(7, 'Petr', 3), (8, 'Daria', 3), (9, 'Anna', 4), (10, 'Aleksandr', 5), (11, 'Inna', 5);

select p.name, c.name from person p join company c on p.company_id = c.id where company_id != 5;

select c.name, count(p.company_id) from 
company c join person p on c.id = p.company_id
group by c.name
having count(p.company_id) = 
(select count(p.company_id) from person p
 group by p.company_id
order by count(p.company_id) desc limit 1);
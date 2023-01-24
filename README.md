Dinamin olarak veri tabanı kaynağı ekleyen örnek koddur.<br>
Ön tanımlı olarak bir veri tabanı ilklenir. Daha sonra gelen isteklere göre yeni veri tabanı bağlantısı oluşturulur.<br>
Yeni gelen istekte ayırt edici olan şey http header bilgisi olacaktır: X-Tenant<br>
X-Tenant değeri veirlmezse ilk başta ilklenen ön tanımlı veri tabanı kullanılacaktır.<br>

```
curl -XGET http://localhost:8080/person
```
bu istek bize ön tanımlı veri tabanındaki person girdilerini verir.
```
curl -XGET http://localhost:8080/person/add-random
```
Bu metod her çağrıldığında rastgele yeni person girdileri oluşturulur ve veri tabanına eklenir.<br>
H2 console'a bağlanarak oluşturulan veriler görülebilir:<br>
http://localhost:8080/h2-console <br>
! JDBC url'ini doğru set etmelisiniz!
<br>
<br>
Şimdi bir tenant ekleyelim, yapmamız gereken sadece X-Tenant header bilgisine bir bilgi girmektir.
```
curl -H 'X-Tenant: dinamik-kiraci' -XGET http://localhost:8080/person
```
Bu istek yeni bir veir tabanı bağlantısı açacak, yeni bir hikari datasource'u oluşturacaktır.
İsteğin sonucunda gelen listenin boş olduğu görülür. Rastgele person ekleyelim:
```
curl -H 'X-Tenant: dinamik-kiraci' -XGET http://localhost:8080/person/add-random
```
Bu istek yapıldığında 'dinamik-kiracı' isimli veri tabanında person tablosuna yeni kayıt eklenecektir.
Ön tanımlı veri tabanında bu kayıtların olmadığı görülür.<br>
Yine H2 Console'a bağlanılarak (JDBC URL'i değişti!) yeni veri tabanında kayıtların oluştuğu görülebilir.
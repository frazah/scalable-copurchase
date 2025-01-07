import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.functions._
import org.apache.spark.storage.StorageLevel
import java.nio.file.{Files, Paths, StandardCopyOption}


object Main {

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("CoPurchaseAnalysis")
      .master("yarn")
      //.master("local[4]") // Usa 'local[*]' per eseguire in locale, 'yarn' per eseguire su un cluster
      //.config("spark.executor.memory", "8g") // Imposta 4GB di RAM per Spark
      //.config("spark.driver.memory", "4g")  // Imposta 2GB di RAM per il driver
      //.config("spark.memory.fraction", "0.8")
      .getOrCreate()

    import spark.implicits._

    val inputPath = "gs://bucket_dummy123//order_products.csv"
    //val inputPath = "../../dataset/dummy.csv"
    //val inputPath = "dataset/order_products.csv"

    val data = spark.read.option("header", "false")
      .csv(inputPath)
      //.limit(1000)
      .toDF("order_id", "product_id")
      //.cache()


    println("DataFrame caricato:")
    data.show(10)

    // Prepara i dati per analizzare le coppie di prodotti per ogni ordine
    val groupedByOrder = data.groupBy("order_id")
      .agg(collect_list("product_id").alias("products"))

    // Stampa il DataFrame raggruppato per ordine
    println("DataFrame raggruppato per order_id con la lista dei prodotti:")
    groupedByOrder.show(10)

    groupedByOrder.persist(StorageLevel.MEMORY_AND_DISK)

    // Funzione per generare le coppie di prodotti
    def generatePairs(products: Seq[String]): Seq[(String, String)] = {
      //println(products)
      val pairs = for {
        i <- 0 until products.length
        j <- i + 1 until products.length
      } yield (products(i), products(j))
      pairs
    }


    // Applica la funzione per generare le coppie di prodotti per ogni ordine
    val pairsRDD: RDD[((String, String), Int)] = groupedByOrder.rdd.flatMap {
      case Row(orderId: String, products: Seq[String]) =>
        generatePairs(products).map { case (product1, product2) =>
          (if (product1 < product2) (product1, product2) else (product2, product1), 1)
        }
    }

    // Conta le occorrenze di ogni coppia di prodotti
    val coPurchaseCounts = pairsRDD.reduceByKey(_ + _).repartition(8)
    .persist(StorageLevel.MEMORY_AND_DISK)

    // Converti l'RDD in un DataFrame
    val coPurchaseDF = coPurchaseCounts.map {
      case ((product1, product2), count) => (product1, product2, count)
    }.toDF("product1", "product2", "count")

    // Stampa lo schema e le prime righe del DataFrame finale
    println("Schema del DataFrame finale:")
    coPurchaseDF.printSchema()

    println("Prime righe del DataFrame finale:")
    coPurchaseDF.show(10)


    //val outputDir = "output/copurchase_results"
    //val outputFile = "output/copurchase_results.csv"

    val outputDir = "gs://bucket_dummy123/results"

    // Scrivi il DataFrame in una directory temporanea come un singolo file
    coPurchaseDF.coalesce(1).write
      .option("header", "false")
      //.mode("overwrite") // Sovrascrive se il file o la directory esiste già
      .csv(outputDir)

    /*

    // Trova il file CSV generato nella directory temporanea
    val tempFile = Files.list(Paths.get(outputDir))
      .filter(path => path.getFileName.toString.startsWith("part-") && path.getFileName.toString.endsWith(".csv"))
      .findFirst()
      .orElseThrow(() => new RuntimeException("File CSV non trovato nella directory temporanea"))

    // Rinomina e sposta il file CSV nella posizione desiderata
    Files.move(tempFile, Paths.get(outputFile), StandardCopyOption.REPLACE_EXISTING)

    // Elimina la directory temporanea
    Files.walk(Paths.get(outputDir))
      .sorted((path1, path2) => path2.compareTo(path1)) // Ordina in ordine inverso per cancellare i file prima delle directory
      .forEach(path => Files.delete(path))

     */

    println("DONE")


    // Ferma la sessione Spark
    spark.stop()
  }
}

# Copurchase analysis

## Istruzioni per l’Esecuzione
- Clonare il repository GitHub:
`git clone https://github.com/frazah/scalable-copurchase.git`


- Caricare il file “order_products.csv” su un bucket di Google Cloud Storage


- Compilare il progetto e generare il file JAR:
`sbt reload ` 
`sbt clean`
`sbt compile`
`sbt assembly` Il file JAR sarà generato nella cartella “target”


- Caricare il file JAR su un bucket di Google Cloud Storage.


- Creare un cluster Dataproc su Google Cloud tramite il comando
`gcloud dataproc clusters create dummy-cluster --region=us-central1 --num-workers 4 --master-boot-disk-size 240 --worker-boot-disk-size 240`


- Eseguire il job utilizzando il comando
`gcloud dataproc jobs submit spark --cluster=dummy-cluster --region=us-central1  --jar=gs://bucket_dummy123//scalable-copurchase.jar`

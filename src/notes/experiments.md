### 10000 max samples
```
2017-10-28 01:16:35  INFO  r.crossValidation.FoldSplitStrategy - Testing 54017 items against 5983 items
2017-10-28 01:16:35  INFO  r.crossValidation.CrossValidator - Building model with 54017 items
2017-10-28 01:16:35  INFO  renegade.util.InputPairSampler - Sampling up to 10000 from 54017 training pairs
2017-10-28 01:16:35  INFO  renegade.MetricSpace - Average distance of distance pairs is 0.9015
2017-10-28 01:16:41  INFO  renegade.MetricSpace - 784 modelBuilders distanceModelList built.
2017-10-28 01:16:52  INFO  renegade.MetricSpace - Iteration #0, RMSE: 0.2972599314777891
2017-10-28 01:17:08  INFO  renegade.MetricSpace - Iteration #1, RMSE: 0.28327356379694485
2017-10-28 01:17:24  INFO  renegade.MetricSpace - Iteration #2, RMSE: 0.28597269739342096
2017-10-28 01:17:39  INFO  renegade.MetricSpace - Terminating refinement because RMSE didn't improve
2017-10-28 01:24:57  INFO  r.crossValidation.CrossValidator - Testing model with 5983 items
2017-10-28 01:40:19  INFO  r.crossValidation.CrossValidator - Tested on 5983 test datums, loss is 0.8499080728731405
```

### 1000 max samples
2017-10-28 12:33:38  INFO  r.crossValidation.FoldSplitStrategy - Testing 53915 items against 6085 items
2017-10-28 12:33:38  INFO  r.crossValidation.CrossValidator - Building model with 53915 items
2017-10-28 12:33:38  INFO  renegade.util.InputPairSampler - Sampling up to 1000 from 53915 training pairs
2017-10-28 12:33:38  INFO  renegade.MetricSpace - Average distance of distance pairs is 0.914
2017-10-28 12:33:39  INFO  renegade.MetricSpace - 784 modelBuilders distanceModelList built.
2017-10-28 12:33:40  INFO  renegade.MetricSpace - Iteration #0, RMSE: 0.27949492798338793
2017-10-28 12:33:42  INFO  renegade.MetricSpace - Iteration #1, RMSE: 0.2638649023907009
2017-10-28 12:33:43  INFO  renegade.MetricSpace - Iteration #2, RMSE: 0.2671703784383156
2017-10-28 12:33:44  INFO  renegade.MetricSpace - Terminating refinement because RMSE didn't improve
2017-10-28 12:39:50  INFO  r.crossValidation.CrossValidator - Testing model with 6085 items
2017-10-28 12:39:50  INFO  renegade.datasets.mnist.Mnist - Tested 1 values
2017-10-28 12:39:51  INFO  renegade.datasets.mnist.Mnist - Tested 2 values
2017-10-28 12:39:52  INFO  renegade.datasets.mnist.Mnist - Tested 4 values
2017-10-28 12:39:52  INFO  renegade.datasets.mnist.Mnist - Tested 8 values
2017-10-28 12:39:53  INFO  renegade.datasets.mnist.Mnist - Tested 16 values
2017-10-28 12:39:56  INFO  renegade.datasets.mnist.Mnist - Tested 32 values
2017-10-28 12:40:00  INFO  renegade.datasets.mnist.Mnist - Tested 64 values
2017-10-28 12:40:11  INFO  renegade.datasets.mnist.Mnist - Tested 128 values
2017-10-28 12:40:28  INFO  renegade.datasets.mnist.Mnist - Tested 256 values
2017-10-28 12:41:05  INFO  renegade.datasets.mnist.Mnist - Tested 512 values
2017-10-28 12:42:09  INFO  renegade.datasets.mnist.Mnist - Tested 1024 values
2017-10-28 12:44:31  INFO  renegade.datasets.mnist.Mnist - Tested 2048 values
2017-10-28 12:49:03  INFO  renegade.datasets.mnist.Mnist - Tested 4096 values
2017-10-28 12:53:20  INFO  r.crossValidation.CrossValidator - Tested on 6085 test datums, correctly classified proportion is 0.8596548890714872